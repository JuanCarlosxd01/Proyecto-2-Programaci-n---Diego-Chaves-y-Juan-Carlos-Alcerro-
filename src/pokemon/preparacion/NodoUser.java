
package pokemon.preparacion;

public class NodoUser {
    private User dato;
    private NodoUser siguiente;
    public NodoUser(User dato){
        this.dato=dato;
        this.siguiente=null;
    }

    public User getDato() {
        return dato;
    }

    public void setDato(User dato) {
        this.dato = dato;
    }

    public NodoUser getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoUser siguiente) {
        this.siguiente = siguiente;
    }
    
    
}
