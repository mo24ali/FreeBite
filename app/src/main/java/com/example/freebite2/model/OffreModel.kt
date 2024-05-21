package com.example.freebite2.model

import android.os.Parcel
import android.os.Parcelable

data class OffreModel(
    var providerID: String? = null,
    var offerID: String? = null,
    var nameoffre: String? = null,
    var details: String? = null,
    var duration: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var pictureUrl: String? = null,
    var provider: User? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString()
    )

    constructor() : this(null, null, null, null, null, null,null, null)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(providerID)
        parcel.writeString(nameoffre)
        parcel.writeString(details)
        parcel.writeString(duration)
        parcel.writeDouble(latitude ?: 0.0)
        parcel.writeDouble((longitude ?: 0.0))
        parcel.writeString(pictureUrl)
    }
  //  public fun getdistance(oid:String) : String{ return "???km"}

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OffreModel> {
        override fun createFromParcel(parcel: Parcel): OffreModel {
            return OffreModel(parcel)
        }


        override fun newArray(size: Int): Array<OffreModel?> {
            return arrayOfNulls(size)
        }
    }
}