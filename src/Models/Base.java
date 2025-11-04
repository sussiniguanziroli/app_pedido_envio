package Models;

public abstract class Base {
    private int id;
    private Boolean eliminado;
    
    public Base(int id, Boolean eliminado){
        this.id = id;
        this.eliminado = eliminado;
    }
    
    public int getId(){
        return id;
    }
    
    public void setId(){
        this.id = id;
    }
    
    public Boolean isEliminado(){
        return eliminado;
    }
    
    public void setEliminado(Boolean eliminado){
        this.eliminado = eliminado;
    }
    
    public Base(){}
}
