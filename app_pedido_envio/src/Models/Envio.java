package Models;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad Envio (Clase B en la relación 1→1).
 * Representa los datos de envío de un pedido.
 * 
 * IMPORTANTE: Envio NO conoce a Pedido (relación unidireccional).
 */
public class Envio extends Base {
    private String tracking;
    private EmpresaEnvio empresa;
    private TipoEnvio tipo;
    private Double costo;
    private LocalDate fechaDespacho;
    private LocalDate fechaEstimada;
    private EstadoEnvio estado;
    
    // Constructor vacío (OBLIGATORIO)
    public Envio() {
        super();
    }
    
    // Constructor completo para reconstruir desde BD
    public Envio(int id, String tracking, EmpresaEnvio empresa, 
                 TipoEnvio tipo, Double costo, LocalDate fechaDespacho,
                 LocalDate fechaEstimada, EstadoEnvio estado) {
        super(id, false);
        this.tracking = tracking;
        this.empresa = empresa;
        this.tipo = tipo;
        this.costo = costo;
        this.fechaDespacho = fechaDespacho;
        this.fechaEstimada = fechaEstimada;
        this.estado = estado;
    }
    
    // Getters y Setters (TODOS)
    public String getTracking() { return tracking; }
    public void setTracking(String tracking) { this.tracking = tracking; }
    
    public EmpresaEnvio getEmpresa() { return empresa; }
    public void setEmpresa(EmpresaEnvio empresa) { this.empresa = empresa; }
    
    public TipoEnvio getTipo() { return tipo; }
    public void setTipo(TipoEnvio tipo) { this.tipo = tipo; }
    
    public Double getCosto() { return costo; }
    public void setCosto(Double costo) { this.costo = costo; }
    
    public LocalDate getFechaDespacho() { return fechaDespacho; }
    public void setFechaDespacho(LocalDate fechaDespacho) { 
        this.fechaDespacho = fechaDespacho; 
    }
    
    public LocalDate getFechaEstimada() { return fechaEstimada; }
    public void setFechaEstimada(LocalDate fechaEstimada) { 
        this.fechaEstimada = fechaEstimada; 
    }
    
    public EstadoEnvio getEstado() { return estado; }
    public void setEstado(EstadoEnvio estado) { this.estado = estado; }
    
    @Override
    public String toString() {
        return "Envio{" +
                "id=" + getId() +
                ", tracking='" + tracking + '\'' +
                ", empresa=" + empresa +
                ", tipo=" + tipo +
                ", costo=" + costo +
                ", fechaDespacho=" + fechaDespacho +
                ", fechaEstimada=" + fechaEstimada +
                ", estado=" + estado +
                ", eliminado=" + isEliminado() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Envio envio = (Envio) o;
        return Objects.equals(tracking, envio.tracking);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(tracking);
    }
}