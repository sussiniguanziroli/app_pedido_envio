package Models;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Clase que representa un pedido en el sistema.
 * Contiene el enum EstadoPedido.
 * Extiende de Base para heredar id y eliminado.
 * 
 * Relación: Un Pedido puede tener un Envio (1→1 unidireccional)
 * IMPORTANTE: Pedido conoce a Envio, pero Envio NO conoce a Pedido
 */
public class Pedido extends Base {
    
    // ============================================
    // ENUM: EstadoPedido
    // ============================================
    
    /**
     * Enum que representa los estados posibles de un pedido.
     * Valores permitidos: NUEVO, FACTURADO, ENVIADO
     * 
     * Flujo típico: NUEVO → FACTURADO → ENVIADO
     */
    public enum EstadoPedido {
        NUEVO,
        FACTURADO,
        ENVIADO;
        
        /**
         * Convierte un String a EstadoPedido de forma segura.
         * 
         * @param valor String con el estado del pedido
         * @return EstadoPedido correspondiente o null si no es válido
         */
        public static EstadoPedido fromString(String valor) {
            if (valor == null || valor.trim().isEmpty()) {
                return null;
            }
            try {
                return EstadoPedido.valueOf(valor.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
    
    // ============================================
    // ATRIBUTOS DE LA CLASE PEDIDO
    // ============================================
    
    /**
     * Número único del pedido.
     * Constraint en BD: NOT NULL, UNIQUE, máx. 20 caracteres
     * Ejemplo: "PED-2025-0001"
     */
    private String numero;
    
    /**
     * Fecha en que se realizó el pedido.
     * Constraint en BD: NOT NULL
     */
    private LocalDate fecha;
    
    /**
     * Nombre del cliente que realizó el pedido.
     * Constraint en BD: NOT NULL, máx. 120 caracteres
     */
    private String clienteNombre;
    
    /**
     * Monto total del pedido en pesos.
     * Constraint en BD: NOT NULL, DECIMAL(12,2), CHECK >= 0
     */
    private Double total;
    
    /**
     * Estado actual del pedido.
     * Constraint en BD: NOT NULL, DEFAULT 'NUEVO'
     */
    private EstadoPedido estado;
    
    /**
     * Envío asociado al pedido (relación 1→1 unidireccional).
     * Puede ser null si el pedido aún no tiene envío asignado.
     * 
     * IMPORTANTE: Esta es la única referencia entre Pedido y Envio.
     * Envio NO tiene referencia a Pedido (unidireccional).
     */
    private Envio envio;

    // ============================================
    // CONSTRUCTORES
    // ============================================
    
    /**
     * Constructor vacío.
     * Usado al crear instancias nuevas antes de setear valores.
     */
    public Pedido() {
        super();
        this.estado = EstadoPedido.NUEVO;  // Estado por defecto
    }

    /**
     * Constructor sin envío (pedido recién creado).
     * Usado para crear pedidos que aún no tienen envío asignado.
     * 
     * @param numero Número único del pedido
     * @param fecha Fecha del pedido
     * @param clienteNombre Nombre del cliente
     * @param total Monto total del pedido
     * @param estado Estado inicial del pedido
     */
    public Pedido(String numero, LocalDate fecha, String clienteNombre, 
                  Double total, EstadoPedido estado) {
        super();
        this.numero = numero;
        this.fecha = fecha;
        this.clienteNombre = clienteNombre;
        this.total = total;
        this.estado = estado;
    }

    /**
     * Constructor completo con envío (sin id, se genera automáticamente).
     * Usado para crear pedidos con envío ya asignado.
     * 
     * @param numero Número único del pedido
     * @param fecha Fecha del pedido
     * @param clienteNombre Nombre del cliente
     * @param total Monto total del pedido
     * @param estado Estado del pedido
     * @param envio Envío asociado (puede ser null)
     */
    public Pedido(String numero, LocalDate fecha, String clienteNombre, 
                  Double total, EstadoPedido estado, Envio envio) {
        this(numero, fecha, clienteNombre, total, estado);
        this.envio = envio;
    }

    // ============================================
    // GETTERS Y SETTERS
    // ============================================
    
    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public Envio getEnvio() {
        return envio;
    }

    public void setEnvio(Envio envio) {
        this.envio = envio;
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================
    
    /**
     * Verifica si el pedido tiene un envío asociado.
     * 
     * @return true si tiene envío, false si envio es null
     */
    public boolean tieneEnvio() {
        return envio != null;
    }
    
    /**
     * Verifica si el pedido es nuevo (recién creado).
     * 
     * @return true si estado es NUEVO
     */
    public boolean esNuevo() {
        return estado == EstadoPedido.NUEVO;
    }
    
    /**
     * Verifica si el pedido está facturado.
     * 
     * @return true si estado es FACTURADO
     */
    public boolean estaFacturado() {
        return estado == EstadoPedido.FACTURADO;
    }
    
    /**
     * Verifica si el pedido está enviado.
     * 
     * @return true si estado es ENVIADO
     */
    public boolean estaEnviado() {
        return estado == EstadoPedido.ENVIADO;
    }
    
    /**
     * Obtiene el código de tracking del envío asociado.
     * 
     * @return tracking del envío o null si no tiene envío
     */
    public String getTrackingEnvio() {
        return tieneEnvio() ? envio.getTracking() : null;
    }
    
    /**
     * Verifica si el pedido tiene número válido.
     * 
     * @return true si número no es null ni vacío
     */
    public boolean tieneNumeroValido() {
        return numero != null && !numero.trim().isEmpty();
    }

    // ============================================
    // EQUALS, HASHCODE, TOSTRING
    // ============================================
    
    /**
     * Compara dos pedidos por su número (identificador de negocio).
     * Dos pedidos son iguales si tienen el mismo número.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(numero, pedido.numero);
    }

    /**
     * Genera hashcode basado en número.
     */
    @Override
    public int hashCode() {
        return Objects.hash(numero);
    }

    /**
     * Representación en String del pedido.
     * Muestra todos los campos principales para debugging.
     */
    @Override
    public String toString() {
        return String.format(
            "Pedido{id=%d, numero='%s', fecha=%s, cliente='%s', total=%.2f, " +
            "estado=%s, envio=%s, eliminado=%s}", 
            getId(), 
            numero, 
            fecha, 
            clienteNombre, 
            total, 
            estado, 
            tieneEnvio() ? envio.getTracking() : "Sin envío",
            getEliminado()
        );
    }
}