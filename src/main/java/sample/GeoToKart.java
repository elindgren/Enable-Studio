/**
 * Created by simon on 2017-07-04.
 */
package sample;

public class GeoToKart {
    final double a = 6378137.0; // Semi-major axis of Earth (m)
    final double b = 6356752.314245; // Semi-minor axis of Earth (m)
    final double e = Math.sqrt(1-Math.pow((b/a),2));
    final double degToRad = Math.PI/180.0;
    double x0;
    double y0;
    double z0;
    //Angles in degrees
    double latitude0;
    double longitude0;
    double altitude0;
    double phi0;
    ReadSerialPort RSP;

    public GeoToKart(double latitude0, double longitude0, double altitude0) {
        this.latitude0 = latitude0;
        this.longitude0 = longitude0;
        this.altitude0 = altitude0;
        phi0 = phi(this.latitude0);
        x0 = x(R(this.latitude0), this.latitude0, this.longitude0, this.altitude0);
        y0 = y(R(this.latitude0), this.latitude0, this.longitude0, this.altitude0);
        z0 = z(R(this.latitude0), this.latitude0, this.altitude0);

    }
    public GeoToKart(ReadSerialPort RSP, double latitude0, double longitude0, double altitude0) {
        this.RSP = RSP;
        this.latitude0 = latitude0;
        this.longitude0 = longitude0;
        this.altitude0 = altitude0;
        phi0 = phi(this.latitude0);
        x0 = x(R(this.latitude0), this.latitude0, this.longitude0, this.altitude0);
        y0 = y(R(this.latitude0), this.latitude0, this.longitude0, this.altitude0);
        z0 = z(R(this.latitude0), this.latitude0, this.altitude0);
    }

    private double phi(double latitude){
        return 180.0/Math.PI*Math.atan((b/a)*Math.tan(degToRad*latitude)); //degrees
    }
    public double R(double latitude) { //Geodetic radii
        return a/Math.sqrt(1 - Math.pow((e*Math.sin(degToRad*latitude)),2));
    }

    //#################### Earth Geodetic Coordinates to Earth Centre Cartesian transformations ########################

    public double x(double R, double latitude, double longitude, double altitude) {
        return (R + altitude)*Math.cos(degToRad*latitude)*Math.cos(degToRad*longitude); // angles in degrees
    }
    public double y(double R, double latitude, double longitude, double altitude) {
        return (R + altitude)*Math.cos(degToRad*latitude)*Math.sin(degToRad*longitude);
    }
    public  double z(double R, double latitude, double altitude) {
        return (Math.pow((b/a),2)*R + altitude)*Math.sin(degToRad*latitude);
    }
    //#################################################################################################################

    //################ Earth Centre Cartesian transformations to Earth Local Cartesian transformations #################

    public double xLocal(double R, double latitude, double longitude, double altitude) {
        return (x(R, latitude, longitude, altitude) - x0)*(-Math.sin(degToRad*longitude))
                + (y(R, latitude, longitude, altitude) - y0)*Math.cos(degToRad*longitude);
    }
    public double yLocal(double R, double latitude, double longitude, double altitude) {
        return (x(R, latitude, longitude, altitude) - x0)*Math.sin(degToRad*phi(latitude))*(-Math.cos(degToRad*longitude))
                + (y(R, latitude, longitude, altitude) - y0)*(-Math.sin(degToRad*phi(latitude)))*Math.sin(degToRad*longitude)
                + (z(R, latitude, altitude) - z0)*Math.cos(degToRad*phi(latitude));
    }
    public double zLocal(double R, double latitude, double longitude, double altitude) {
        return (x(R, latitude, longitude, altitude) - x0)*Math.cos(degToRad*phi(latitude))*Math.cos(degToRad*longitude)
                + (y(R, latitude, longitude, altitude) - y0)*Math.cos(degToRad*phi(latitude))*Math.sin(degToRad*longitude)
                + (z(R, latitude, altitude) - z0)*Math.sin(degToRad*phi(latitude));
    }
    //#################################################################################################################

    public void setX0(double x0){
        this.x0 = x0;
    }
    public void setY0(double y0){
        this.y0 = y0;
    }
    public void setZ0(double z0){
        this.z0 = z0;
    }
    public void setLatitude0(double latitude0) {
        this.latitude0 = latitude0;
    }
    public void setLongitude0(double longitude0) {
        this.longitude0 = longitude0;
    }
    public void setAltitude0(double altitude0) {
        this.altitude0 = altitude0;
    }
    public void setPhi0(double phi0) {
        this.phi0 = phi0;
    }
    public double getX0() {
        return x0;
    }
    public double getY0() {
        return y0;
    }
    public double getZ0() {
        return z0;
    }
    public double getLatitude0() {
        return latitude0;
    }
    public double getLongitude0() {
        return longitude0;
    }
    public double getAltitude0() {
        return altitude0;
    }
    public double getPhi0() {
        return phi0;
    }
    public static void main(String[] args) {
        GeoToKart test = new GeoToKart(57.430981, 12.037498, 59.1);
        System.out.println(test.getX0());
        System.out.println(test.getY0());
        System.out.println(test.getZ0());
        System.out.println(Math.sqrt(Math.pow(test.getX0(), 2) + Math.pow(test.getY0(), 2) + Math.pow(test.getZ0(), 2)));
        System.out.println(test.getLongitude0());
        System.out.println("East: " + test.xLocal(test.R(57.430771), 57.430771, 12.037641, 38.00));
        System.out.println("North: " + test.yLocal(test.R(57.430771), 57.430771, 12.037641, 38.00));
        System.out.println("Vertical: " + test.zLocal(test.R(57.430771), 57.430771, 12.037641, 38.00));

    }
}
