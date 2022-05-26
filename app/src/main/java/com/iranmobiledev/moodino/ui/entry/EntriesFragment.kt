package com.iranmobiledev.moodino.ui.entry


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.iranmobiledev.moodino.R
import com.iranmobiledev.moodino.base.BaseFragment
import com.iranmobiledev.moodino.data.*
import com.iranmobiledev.moodino.databinding.FragmentEntriesBinding
import com.iranmobiledev.moodino.listener.AddEntryCardViewListener
import com.iranmobiledev.moodino.listener.DialogEventListener
import com.iranmobiledev.moodino.listener.EmojiClickListener
import com.iranmobiledev.moodino.listener.EntryEventLister
import com.iranmobiledev.moodino.service.EmojiNotification
import com.iranmobiledev.moodino.ui.calendar.toolbar.ChangeCurrentMonth
import com.iranmobiledev.moodino.ui.entry.adapter.BOTTOM_TEXT
import com.iranmobiledev.moodino.ui.entry.adapter.ENTRY_CARD
import com.iranmobiledev.moodino.ui.entry.adapter.EntryContainerAdapter
import com.iranmobiledev.moodino.utlis.*
import io.github.persiancalendar.calendar.AbstractDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat


class EntriesFragment : BaseFragment(), EntryEventLister, ChangeCurrentMonth,
    AddEntryCardViewListener,
    KoinComponent, EmojiClickListener {

    private lateinit var binding: FragmentEntriesBinding
    private lateinit var recyclerView: RecyclerView
    private val viewModel: EntryViewModel by viewModel()
    private val adapter: EntryContainerAdapter by inject()
    private var emptyStateEnum: EmptyStateEnum = EmptyStateEnum.INVISIBLE
    private val sharePref: SharedPreferences by inject()
    private val args: EntriesFragmentArgs by navArgs()
    private lateinit var layoutManager: LinearLayoutManager
    private var userScroll = false
    private var newEntryScroll = false
    private var fromToolbarClick = false
    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharePref.edit().putBoolean(FIRST_ENTER, false).apply()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEntriesBinding.inflate(inflater, container, false)
        setupUi()
        setupObserver()
        return binding.root
    }

    private fun setupUi() {
        adapter.specifyDay = -1
        adapter.create(
            requireContext().applicationContext, this,
            mutableListOf(),
            this,
            sharePref.getInt(LANGUAGE, 1)
        )
        binding.mainToolbar.initialize(this)
        recyclerView = binding.entriesContainerRv
        recyclerView.itemAnimator = null
        layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }

    private fun setupObserver() {
        val persianDate = PersianDate()
        val today = EntryDate(persianDate.shYear, persianDate.shMonth, persianDate.shDay)
        viewModel.getEntries().observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val data = it as MutableList<RecyclerViewData>
                val date = data.find { it.date == today }
                val bottomText = data.find { it.date == EntryDate(0,0,0) }
                if (date == null) {
                    val cardItem = RecyclerViewData(
                        entries = listOf(),
                        adapter = null,
                        id = "card",
                        date = EntryDate(5000, 13, 32),
                        viewType = ENTRY_CARD

                    )
                    if (!data.contains(cardItem))
                        data.add(0, cardItem)
                }
                if(bottomText == null){
                    val bottomTextData = RecyclerViewData(
                        entries = mutableListOf(),
                        date = EntryDate(0, 0, 0),
                        viewType = BOTTOM_TEXT
                    )
                    data.add(bottomTextData)
                }
                adapter.setData(data)

            } else {
                if (emptyStateEnum == EmptyStateEnum.INVISIBLE) binding.emptyStateContainer.visibility =
                    View.VISIBLE
                else if (it.isNotEmpty() && emptyStateEnum == EmptyStateEnum.VISIBLE) binding.emptyStateContainer.visibility =
                    View.GONE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.emojisView.setEmojiClickListener(this)
        getEmojiFromNotification()
        val newEntry = EntriesFragmentArgs.fromBundle(requireArguments()).newEntry
        newEntry?.let {
            newEntryScroll = true
            scroll(it.date)
            requireArguments().clear()
        }

        var scrollUpPosition = -1
        var scrollDownPosition = -1
        var state = -1
        binding.entriesContainerRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {


            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                state = newState
                userScroll = state != RecyclerView.SCROLL_STATE_IDLE
                if(newState == RecyclerView.SCROLL_STATE_IDLE)
                    newEntryScroll = false
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                println("y is y : $dy")
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        if (dy > 0) {
                            if(!fromToolbarClick)
                            if(!newEntryScroll)
                            if (state == RecyclerView.SCROLL_STATE_DRAGGING || state == RecyclerView.SCROLL_STATE_SETTLING) {
                                val position = layoutManager.findFirstVisibleItemPosition()
                                if (position != -1) {
                                    if (position != scrollUpPosition) {
                                        val date = adapter.findDataWithPosition(position)
                                        if (date.day <= 31)
                                            binding.mainToolbar.goToMonth(date)
                                        scrollUpPosition = position
                                    }
                                }
                            }
                        }
                        if (dy < 0) {
                            if(!fromToolbarClick)
                            if(!newEntryScroll)
                            if (state == RecyclerView.SCROLL_STATE_DRAGGING || state == RecyclerView.SCROLL_STATE_SETTLING) {
                                val position = layoutManager.findFirstVisibleItemPosition()
                                if (position != -1) {
                                    if (position != scrollDownPosition) {
                                        val date = adapter.findDataWithPosition(position)
                                        if (date.day <= 31)
                                            binding.mainToolbar.goToMonth(date)
                                        scrollDownPosition = position
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun getEmojiFromNotification() {
        val persianDate = PersianDate()
        if (EmojiNotification.emoji != null){
            val entry = Entry()
            entry.date = EntryDate(persianDate.shYear, persianDate.shMonth, persianDate.shDay)
            entry.time = EntryTime(
                PersianDateFormat.format(persianDate, "H"),
                PersianDateFormat.format(persianDate, "i")
            )
            entry.emojiValue = EmojiNotification.emoji!!
            navigateToEntryDetail(entry)
            EmojiNotification.emoji = null
        }
    }

    private fun navigateToEntryDetail(entry: Entry) {
        val bundle = Bundle()
        bundle.putParcelable("entry", entry)
        findNavController().navigate(R.id.action_entriesFragment_to_entryDetailFragment, bundle)
    }

    private fun showDeleteDialog(entry: Entry, deletePosition: Int) {
        val dialog = makeDialog(
            mainText = R.string.dialogMainText,
            subText = R.string.dialogSubText,
            icon = R.drawable.ic_delete,
        )
        dialog.setItemEventListener(object : DialogEventListener {
            override fun clickedItem(itemId: Int) {
                when (itemId) {
                    R.id.rightButton -> {
                        viewModel.deleteEntry(entry)
                        dialog.dismiss()
                    }
                    R.id.leftButton -> {
                        dialog.dismiss()
                    }
                }
            }
        })
        dialog.show(parentFragmentManager, null)
    }

    override fun changeCurrentMonth(date: AbstractDate) {
        lifecycleScope.launch {
            if (!userScroll)
                if(recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE){
                    fromToolbarClick = true
                    scroll(date)
                    delay(500)
                    fromToolbarClick = false
                }
        }
    }

    private fun scroll(date: AbstractDate) {
        val smoothScroller = object : LinearSmoothScroller(requireContext()) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        val mDate = EntryDate(date.year, date.month, date.dayOfMonth)

        val position = adapter.positionOf(mDate, false)
        if (position != -1) {
            smoothScroller.targetPosition = position
            layoutManager.startSmoothScroll(smoothScroller)
        }
    }



    private fun scroll(date: EntryDate) {
        val smoothScroller = object : LinearSmoothScroller(requireContext()) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        lifecycleScope.launchWhenResumed {
            binding.mainToolbar.goToMonth(date)
            delay(1000)
            val position = adapter.positionOf(date, true)
            if (position != -1) {
                smoothScroller.targetPosition = position
                layoutManager.startSmoothScroll(smoothScroller)
            }
        }
    }

    override fun update(entry: Entry) {
        val action = EntriesFragmentDirections.actionEntriesFragmentToEntryDetailFragment(
            edit = true,
            entry = entry
        )
        findNavController().navigate(action)
    }

    override fun delete(entry: Entry): Boolean {
        val position = adapter.entryPositionOf(entry)
        lifecycleScope.launchWhenResumed { showDeleteDialog(entry, position) }
        return true
    }

    override fun onEmojiItemClicked(emojiValue: Int) {
        val persianDate = PersianDate()
        val entry = Entry()
        entry.date = EntryDate(persianDate.shYear, persianDate.shMonth, persianDate.shDay)
        entry.time = EntryTime(
            PersianDateFormat.format(persianDate, "H"),
            PersianDateFormat.format(persianDate, "i")
        )
        entry.emojiValue = emojiValue
        navigateToEntryDetail(entry)
    }

    override fun onAddEntryCardClicked() {
        val persianDate = PersianDate()
        val date = EntryDate(
            persianDate.shYear,
            persianDate.shMonth,
            persianDate.shDay
        )
        val time = EntryTime(persianDate.hour.toString(), persianDate.minute.toString())
        val action = EntriesFragmentDirections.actionEntriesFragmentToAddEntryFragment(
            date = date,
            time = time
        )
        findNavController().navigate(action)
    }

}