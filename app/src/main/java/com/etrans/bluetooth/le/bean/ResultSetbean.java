package com.etrans.bluetooth.le.bean;

public class ResultSetbean {

    boolean phone_Num;
    boolean vin_Num;
    boolean car_Num;
    boolean ID_Num;
    boolean IP1;//05
    boolean port1;//0a
    boolean software_ver;//0f
    boolean hardware_ver;//10


    public boolean isPhone_Num() {
        return phone_Num;
    }

    public void setPhone_Num(boolean phone_Num) {
        this.phone_Num = phone_Num;
    }

    public boolean isVin_Num() {
        return vin_Num;
    }

    public void setVin_Num(boolean vin_Num) {
        this.vin_Num = vin_Num;
    }

    public boolean isCar_Num() {
        return car_Num;
    }

    public void setCar_Num(boolean car_Num) {
        this.car_Num = car_Num;
    }

    public boolean isID_Num() {
        return ID_Num;
    }

    public void setID_Num(boolean ID_Num) {
        this.ID_Num = ID_Num;
    }

    public boolean isIP1() {
        return IP1;
    }

    public void setIP1(boolean IP1) {
        this.IP1 = IP1;
    }

    public boolean isPort1() {
        return port1;
    }

    public void setPort1(boolean port1) {
        this.port1 = port1;
    }

    public boolean isSoftware_ver() {
        return software_ver;
    }

    public void setSoftware_ver(boolean software_ver) {
        this.software_ver = software_ver;
    }

    public boolean isHardware_ver() {
        return hardware_ver;
    }

    public void setHardware_ver(boolean hardware_ver) {
        this.hardware_ver = hardware_ver;
    }
}
