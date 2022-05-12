package com.iranmobiledev.moodino.data

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iranmobiledev.moodino.utlis.EmojiValue
import kotlinx.android.parcel.Parcelize


//TODO warning some of this can be null while adding a entry ! byTayeb.

@Entity(tableName = "table_entry")
@Parcelize
data class Entry(
    @PrimaryKey(autoGenerate = true)
    var id : Int? = null,
    @StringRes var title : Int = -1,
    var note : String = "",
    var activities : List<Activity> = ArrayList(),
    var photoPath : String = "",
    @DrawableRes var icon : Int = 0,
    var date : EntryDate? = null,
    var time : EntryTime? = null,
    var emojiValue: Float= EmojiValue.MEH  // value is between 1 to 5
) : Parcelable

@Parcelize
data class EntryDate(
    val year : Int,
    val month : Int,
    val day : Int,
) : Parcelable

@Parcelize
data class EntryTime(
    val hour : String,
    val minutes : String
) : Parcelable