package Models;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad Pedido (Clase A en la relación 1→1).
 * Representa un pedido de cliente con su envío asociado.
 * 
 * RELACIÓN 1→1 UNIDIRECCIONAL:
 * - Pedido CONOCE a Envio (tiene atributo private Envio envio)
 * - Envio NO conoce a Pedido
 */
public class Pedido extends Base {
    private String numero;
    private LocalDate fecha;
    private String clienteNombre;
    private Double total;
    private EstadoPedido estado;
    
    // ⭐ LA RELACIÓN 1→1 UNIDIRECCIONAL
    private Envio envio;
    
    // Constructor vacío (OBLIGATORIO)
    public Pedido() {
        super();
    }
    
    // Constructor completo para reconstruir desde BD
    public Pedido(int id, String numero, LocalDate fecha, 
                  String clienteNombre, Double total, EstadoPedido estado) {
        super(id, false);
        this.numero = numero;
        this.fecha = fecha;
        this.clienteNombre = clienteNombre;
        this.total = total;
        this.estado = estado;
    }
    
    // Getters y Setters (TODOS)
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { 
        this.clienteNombre = clienteNombre; 
    }
    
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    
    // ⭐ Getter y Setter para la relación 1→1
    public Envio getEnvio() { return envio; }
    public void setEnvio(Envio envio) { this.envio = envio; }
    
    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + getId() +
                ", numero='" + numero + '\'' +
                ", fecha=" + fecha +
                ", clienteNombre='" + clienteNombre + '\'' +
                ", total=" + total +
                ", estado=" + estado +
                ", envio=" + (envio != null ? envio.getTracking() : "sin envío") +
                ", eliminado=" + isEliminado() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(numero, pedido.numero);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(numero);
    }
}