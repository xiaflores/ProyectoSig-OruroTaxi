package com.example.proyectosig.models;

public class Conductor {
    String id;
    String name;
    String email;
    String marca;
    String placa;

    public Conductor(String id, String name, String email, String marca, String placa) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.marca = marca;
        this.placa = placa;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }
}
