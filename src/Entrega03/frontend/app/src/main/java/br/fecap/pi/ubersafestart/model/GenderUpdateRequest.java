// Arquivo: br/fecap/pi/ubersafestart/model/GenderUpdateRequest.java
package br.fecap.pi.ubersafestart.model;

public class GenderUpdateRequest {
    private String genero; // Ex: "FEMININO", "MASCULINO", "OUTROS"

    public GenderUpdateRequest(String genero) {
        this.genero = genero;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }
}