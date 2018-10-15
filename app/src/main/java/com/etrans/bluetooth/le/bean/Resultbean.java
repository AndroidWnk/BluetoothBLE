package com.etrans.bluetooth.le.bean;

public class Resultbean {

    String phone_Num;
    String vin_Num;
    String car_Num;
    String ID_Num;
    String IP1;//05
    String port1;//0a
    String software_ver;//0f
    String hardware_ver;//10


    public String getPhone_Num() {
        return phone_Num;
    }

    public void setPhone_Num(String phone_Num) {
        this.phone_Num = phone_Num;
    }

    public String getVin_Num() {
        return vin_Num;
    }

    public void setVin_Num(String vin_Num) {
        this.vin_Num = vin_Num;
    }

    public String getCar_Num() {
        return car_Num;
    }

    public void setCar_Num(String car_Num) {
        this.car_Num = car_Num;
    }

    public String getID_Num() {
        return ID_Num;
    }

    public void setID_Num(String ID_Num) {
        this.ID_Num = ID_Num;
    }


    public String getIP1() {
        return IP1;
    }

    public void setIP1(String IP1) {
        this.IP1 = IP1;
    }

    public String getPort1() {
        return port1;
    }

    public void setPort1(String port1) {
        this.port1 = port1;
    }

    public String getSoftware_ver() {
        return software_ver;
    }

    public void setSoftware_ver(String software_ver) {
        this.software_ver = software_ver;
    }

    public String getHardware_ver() {
        return hardware_ver;
    }

    public void setHardware_ver(String hardware_ver) {
        this.hardware_ver = hardware_ver;
    }
}
