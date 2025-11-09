/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Main;

/**
 *
 * @author paddy
 */
// Main/TestPedido.java
import Config.DatabaseConnection;
import Service.PedidoServiceImpl;
import Service.EnvioServiceImpl;
import Dao.PedidoDAO;
import Dao.EnvioDAO;
import Models.*;
import java.time.LocalDate;

public class TestPedido {
    public static void main(String[] args) {
        try {
            System.out.println("🔌 Probando conexión...");
            DatabaseConnection.getConnection().close();
            System.out.println("✅ Conexión OK\n");
            
            // Crear servicios
            EnvioDAO envioDAO = new EnvioDAO();
            PedidoDAO pedidoDAO = new PedidoDAO();
            EnvioServiceImpl envioService = new EnvioServiceImpl(envioDAO);
            PedidoServiceImpl pedidoService = new PedidoServiceImpl(pedidoDAO, envioService);
            
            // 1. Listar pedidos existentes
            System.out.println("📋 Pedidos existentes:");
            pedidoService.getAll().forEach(System.out::println);
            
            // 2. Buscar pedido por número
            System.out.println("\n🔍 Buscar PED-2025-0001:");
            Pedido pedido1 = pedidoService.buscarPorNumero("PED-2025-0001");
            System.out.println(pedido1);
            if (pedido1.getEnvio() != null) {
                System.out.println("   ↳ Envío: " + pedido1.getEnvio().getTracking());
            }
            
            // 3. Crear pedido CON envío (transacción)
            System.out.println("\n➕ Creando pedido con envío...");
            Envio nuevoEnvio = new Envio();
            nuevoEnvio.setTracking("TEST-" + System.currentTimeMillis());
            nuevoEnvio.setEmpresa(EmpresaEnvio.ANDREANI);
            nuevoEnvio.setTipo(TipoEnvio.EXPRES);
            nuevoEnvio.setCosto(1200.0);
            nuevoEnvio.setEstado(EstadoEnvio.EN_PREPARACION);
            
            Pedido nuevoPedido = new Pedido();
            nuevoPedido.setNumero("TEST-" + System.currentTimeMillis());
            nuevoPedido.setFecha(LocalDate.now());
            nuevoPedido.setClienteNombre("Test Usuario");
            nuevoPedido.setTotal(25000.0);
            nuevoPedido.setEstado(EstadoPedido.NUEVO);
            nuevoPedido.setEnvio(nuevoEnvio);
            
            pedidoService.crearPedidoConEnvio(nuevoPedido);
            System.out.println("✅ Pedido creado con ID: " + nuevoPedido.getId());
            System.out.println("✅ Envío creado con ID: " + nuevoEnvio.getId());
            
            // 4. Verificar
            System.out.println("\n📋 Total de pedidos ahora:");
            System.out.println("   " + pedidoService.getAll().size() + " pedidos");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}