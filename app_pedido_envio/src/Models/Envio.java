package Models;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Clase que representa un envío en el sistema.
 * Contiene los enums relacionados: EmpresaEnvio, TipoEnvio, EstadoEnvio.
 * Extiende de Base para heredar id y eliminado.
 * 
 * Relación: Un Pedido puede tener un Envio (1→1 unidireccional)
 */
public class Envio extends Base {
    
    // ============================================
    // ENUM: EmpresaEnvio
    // ============================================
    
    /**
     * Enum que representa las empresas de envío disponibles.
     * Valores permitidos: ANDREANI, OCA, CORREO_ARG
     */
    public enum EmpresaEnvio {
        ANDREANI,
        OCA,
        CORREO_ARG;
        
        /**
         * Convierte un String a EmpresaEnvio de forma segura.
         * Acepta mayúsculas, minúsculas y espacios.
         * 
         * @param valor String con el nombre de la empresa
         * @return EmpresaEnvio correspondiente o null si no es válido
         */
        public static EmpresaEnvio fromString(String valor) {
            if (valor == null || valor.trim().isEmpty()) {
                return null;
            }
            try {
                return EmpresaEnvio.valueOf(valor.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
    
    // ============================================
    // ENUM: TipoEnvio
    // ============================================
    
    /**
     * Enum que representa los tipos de envío disponibles.
     * Valores permitidos: ESTANDAR, EXPRES
     */
    public enum TipoEnvio {
        ESTANDAR,
        EXPRES;
        
        /**
         * Convierte un String a TipoEnvio de forma segura.
         * 
         * @param valor String con el tipo de envío
         * @return TipoEnvio correspondiente o null si no es válido
         */
        public static TipoEnvio fromString(String valor) {
            if (valor == null || valor.trim().isEmpty()) {
                return null;
            }
            try {
                return TipoEnvio.valueOf(valor.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
    
    // ============================================
    // ENUM: EstadoEnvio
    // ============================================
    
    /**
     * Enum que representa los estados posibles de un envío.
     * Valores permitidos: EN_PREPARACION, EN_TRANSITO, ENTREGADO
     * 
     * Flujo típico: EN_PREPARACION → EN_TRANSITO → ENTREGADO
     */
    public enum EstadoEnvio {
        EN_PREPARACION,
        EN_TRANSITO,
        ENTREGADO;
        
        /**
         * Convierte un String a EstadoEnvio de forma segura.
         * 
         * @param valor String con el estado del envío
         * @return EstadoEnvio correspondiente o null si no es válido
         */
        public static EstadoEnvio fromString(String valor) {
            if (valor == null || valor.trim().isEmpty()) {
                return null;
            }
            try {
                return EstadoEnvio.valueOf(valor.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
    
    // ============================================
    // ATRIBUTOS DE LA CLASE ENVIO
    // ============================================
    
    /**
     * Código de seguimiento único del envío.
     * Constraint en BD: UNIQUE, máx. 40 caracteres
     */
    private String tracking;
    
    /**
     * Empresa que realiza el envío.
     * Constraint en BD: NOT NULL
     */
    private EmpresaEnvio empresa;
    
    /**
     * Tipo de envío (velocidad de entrega).
     * Constraint en BD: NOT NULL, DEFAULT 'ESTANDAR'
     */
    private TipoEnvio tipo;
    
    /**
     * Costo del envío en pesos.
     * Constraint en BD: NOT NULL, DECIMAL(10,2), CHECK >= 0
     */
    private Double costo;
    
    /**
     * Fecha en que se despachó el envío.
     * Puede ser null si aún no se despachó.
     */
    private LocalDate fechaDespacho;
    
    /**
     * Fecha estimada de entrega.
     * Puede ser null si aún no se calculó.
     * Constraint en BD: debe ser >= fechaDespacho
     */
    private LocalDate fechaEstimada;
    
    /**
     * Estado actual del envío.
     * Constraint en BD: NOT NULL, DEFAULT 'EN_PREPARACION'
     */
    private EstadoEnvio estado;

    // ============================================
    // CONSTRUCTORES
    // ============================================
    
    /**
     * Constructor vacío.
     * Usado al crear instancias nuevas antes de setear valores.
     */
    public Envio() {
        super();
    }

    /**
     * Constructor completo (sin id, se genera automáticamente).
     * Usado para crear envíos con todos los datos conocidos.
     * 
     * @param tracking Código de seguimiento único
     * @param empresa Empresa de envío
     * @param tipo Tipo de envío (ESTANDAR o EXPRES)
     * @param costo Costo del envío
     * @param fechaDespacho Fecha de despacho (puede ser null)
     * @param fechaEstimada Fecha estimada de entrega (puede ser null)
     * @param estado Estado actual del envío
     */
    public Envio(String tracking, EmpresaEnvio empresa, TipoEnvio tipo, 
                 Double costo, LocalDate fechaDespacho, LocalDate fechaEstimada, 
                 EstadoEnvio estado) {
        super();
        this.tracking = tracking;
        this.empresa = empresa;
        this.tipo = tipo;
        this.costo = costo;
        this.fechaDespacho = fechaDespacho;
        this.fechaEstimada = fechaEstimada;
        this.estado = estado;
    }

    // ============================================
    // GETTERS Y SETTERS
    // ============================================
    
    public String getTracking() {
        return tracking;
    }

    public void setTracking(String tracking) {
        this.tracking = tracking;
    }

    public EmpresaEnvio getEmpresa() {
        return empresa;
    }

    public void setEmpresa(EmpresaEnvio empresa) {
        this.empresa = empresa;
    }

    public TipoEnvio getTipo() {
        return tipo;
    }

    public void setTipo(TipoEnvio tipo) {
        this.tipo = tipo;
    }

    public Double getCosto() {
        return costo;
    }

    public void setCosto(Double costo) {
        this.costo = costo;
    }

    public LocalDate getFechaDespacho() {
        return fechaDespacho;
    }

    public void setFechaDespacho(LocalDate fechaDespacho) {
        this.fechaDespacho = fechaDespacho;
    }

    public LocalDate getFechaEstimada() {
        return fechaEstimada;
    }

    public void setFechaEstimada(LocalDate fechaEstimada) {
        this.fechaEstimada = fechaEstimada;
    }

    public EstadoEnvio getEstado() {
        return estado;
    }

    public void setEstado(EstadoEnvio estado) {
        this.estado = estado;
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================
    
    /**
     * Verifica si el envío ha sido entregado.
     * 
     * @return true si estado es ENTREGADO
     */
    public boolean estaEntregado() {
        return estado == EstadoEnvio.ENTREGADO;
    }
    
    /**
     * Verifica si el envío está en tránsito.
     * 
     * @return true si estado es EN_TRANSITO
     */
    public boolean estaEnTransito() {
        return estado == EstadoEnvio.EN_TRANSITO;
    }
    
    /**
     * Verifica si el envío está en preparación.
     * 
     * @return true si estado es EN_PREPARACION
     */
    public boolean estaEnPreparacion() {
        return estado == EstadoEnvio.EN_PREPARACION;
    }
    
    /**
     * Verifica si el tracking está definido.
     * 
     * @return true si tiene tracking, false si es null o vacío
     */
    public boolean tieneTracking() {
        return tracking != null && !tracking.trim().isEmpty();
    }
    
    /**
     * Verifica si las fechas de despacho y estimada están definidas.
     * 
     * @return true si ambas fechas están setadas
     */
    public boolean tieneFechasCompletas() {
        return fechaDespacho != null && fechaEstimada != null;
    }

    // ============================================
    // EQUALS, HASHCODE, TOSTRING
    // ============================================
    
    /**
     * Compara dos envíos por su tracking (identificador de negocio).
     * Dos envíos son iguales si tienen el mismo tracking.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Envio envio = (Envio) o;
        return Objects.equals(tracking, envio.tracking);
    }

    /**
     * Genera hashcode basado en tracking.
     */
    @Override
    public int hashCode() {
        return Objects.hash(tracking);
    }

    /**
     * Representación en String del envío.
     * Muestra todos los campos principales para debugging.
     */
    @Override
    public String toString() {
        return String.format(
            "Envio{id=%d, tracking='%s', empresa=%s, tipo=%s, costo=%.2f, " +
            "fechaDespacho=%s, fechaEstimada=%s, estado=%s, eliminado=%s}", 
            getId(), 
            tracking, 
            empresa, 
            tipo, 
            costo != null ? costo : 0.0,
            fechaDespacho,
            fechaEstimada,
            estado,
            getEliminado()
        );
    }
}