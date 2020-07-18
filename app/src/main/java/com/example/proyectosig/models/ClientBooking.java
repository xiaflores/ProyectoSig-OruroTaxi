package com.example.proyectosig.models;

public class ClientBooking {
    String idClient;
    String idConductor;
    String destination;
    String origin;
    String time;
    String km;
    String status;
    double orginLat;
    double orginLng;
    double destinationLat;
    double destinationLng;

    public ClientBooking(String idClient, String idConductor, String destination, String origin, String time, String km, String status, double orginLat, double orginLng, double destinationLat, double destinationLng) {
        this.idClient = idClient;
        this.idConductor = idConductor;
        this.destination = destination;
        this.origin = origin;
        this.time = time;
        this.km = km;
        this.status = status;
        this.orginLat = orginLat;
        this.orginLng = orginLng;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public String getIdConductor() {
        return idConductor;
    }

    public void setIdConductor(String idConductor) {
        this.idConductor = idConductor;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getOrginLat() {
        return orginLat;
    }

    public void setOrginLat(double orginLat) {
        this.orginLat = orginLat;
    }

    public double getOrginLng() {
        return orginLng;
    }

    public void setOrginLng(double orginLng) {
        this.orginLng = orginLng;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public double getDestinationLng() {
        return destinationLng;
    }

    public void setDestinationLng(double destinationLng) {
        this.destinationLng = destinationLng;
    }
}
