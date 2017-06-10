package ryanwendling.tenniscourtfinder;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by wendlir on 5/23/17.
 */
public class markLatLng {

    private double _lat;
    private double _lng;

    public markLatLng() {

    }

    public markLatLng(double alat, double alng) {
        this._lat = alat;
        this._lng = alng;
    }

    public void setLat(double lat) {
        this._lat = lat;
    }

    public double getLat() {
        return this._lat;
    }

    public void setLng(double lng) {
        this._lng = lng;
    }

    public double getLng() {
        return this._lng;
    }
}
