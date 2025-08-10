package Controller;

import Model.Client;
import Model.MenuItem;
import ModelDAOImpl.MenuItemDAOImpl;
import Model.User;
import ModelDAOImpl.ClientDAOImpl;
import ModelDAOImpl.UserDAOImpl;
import static io.github.pixee.security.jakarta.PathValidator.validateDispatcherPath;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.types.ObjectId;
import org.javatuples.Pair;

/**
 *
 * @author joseignacio
 */
@WebServlet(name = "Controller", urlPatterns = {"/Controller"})
public class Controller extends HttpServlet {
    
    /*Atributes*/
    String loginViewRoute = "";
    String confirmationRoute = "Views/new.jsp";
    String invoicingViewRoute = "Views/Invoice/newInvoice.jsp";
    String adminMenuRoute = "Views/menu/adminMenu.jsp";
    String adminUsersRoute = "Views/users/adminUsers.jsp";
    String updateUserViewRoute = "Views/users/updateUser.jsp";
    String createUserViewRoute = "Views/users/createUser.jsp";
    String menuItemRoute = "Views/menu/menuItem.jsp";
    String adminMenuItemsRoute = "Views/menu/adminMenuItems.jsp";
    String createMenuItemViewRoute= "Views/menu/createMenuItem.jsp";
    String updatMenuItemViewRoute = "Views/menu/updateMenuItem.jsp";
    String adminInvoiceViewRoute = "Views/Invoice/adminInvoices.jsp";
    String updateInvoiceViewRoute = "Views/Invoice/updateInvoice.jsp";;
    String adminClientsViewRoute = "Views/Clients/adminClients.jsp";
    String updateClientViewRoute = "Views/Clients/updateClient.jsp";
    String createClientViewRoute = "Views/Clients/createClient.jsp";

    /* TODO  Routes to Views*/
    
    // Objects
    User user = new User();
    MenuItem menuItem = new MenuItem();
    
    // DAOs
    UserDAOImpl userDAO = new UserDAOImpl();
    MenuItemDAOImpl menuItemDAO = new MenuItemDAOImpl ();
    ClientDAOImpl clientDAO = new ClientDAOImpl();
    
        

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try ( PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Controller</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Controller at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // The path to the view that wll be sent based on action received
        String viewToSend = "";
                
        // Code modified from Michael Cobacango's tutorial
        // https://www.youtube.com/playlist?list=PLLV74Oll0_H1NdtWeGRjBLWrAUjSYzKNw
        RequestDispatcher view = request.getRequestDispatcher(viewToSend);
        view.forward(request, response);
        
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        
        // The path to the view that wll be sent based on action received
        String viewToSend = "";
        String action = request.getParameter("action");
        
        switch (action) {
            case "login":
                
                // Clean User
                user = null;
                
                String usernameEntered = request.getParameter("username");
                
                Pair userExistsAnswer = userDAO.userExists(usernameEntered);
                
                if((boolean) userExistsAnswer.getValue0()) // userExists returns a Tuple<Boolean, ObjectId>
                {
                    System.out.println("The user exists!");
                    
                    ObjectId userObjectId = (ObjectId) userExistsAnswer.getValue1();
                    
                    // Verify password with Bcrypt hash on DB
                    String passwordEntered = request.getParameter("password");
                    boolean valid = userDAO.passwordIsValid(userObjectId, passwordEntered);
                    
                    if(valid)
                    {
                        String userType = userDAO.listUser(userObjectId).getType();
                        System.out.println("The user is: " + userType );
                        System.out.println("Password verified! Sending to role's respective view");
                        
                        // User Types Views
                        if (userType.equals("admin")){
                            viewToSend = adminMenuRoute;
                        }
                        else if(userType.equals("cashier")){
                            viewToSend = invoicingViewRoute;
                        }
                    }
                    else
                    {
                        System.out.println("Wrong password");
                        request.setAttribute("error","Usuario o Contraseña Incorrectos");
                        viewToSend = "";
                    }
                }
                else{
                    System.out.println("User does not exist");
                    request.setAttribute("error","Usuario o Contraseña Incorrectos");
                    viewToSend = "";
                }
            break;
            case "goToCreateMenuItemView":
                viewToSend = createMenuItemViewRoute;
            break;
            case "updateMenuItem":
                String codeMenuItemToUpdate = request.getParameter("code");
                try {
                    MenuItem menuItem = menuItemDAO.listMenuItem(codeMenuItemToUpdate);
                    
                    menuItem.setStatus(request.getParameter("status"));
                    menuItem.setCategory(request.getParameter("category"));
                    menuItem.setName(request.getParameter("name"));
                    menuItem.setPrice(new BigDecimal(request.getParameter("price")));
                    String paysTaxesNew = request.getParameter("paysTaxes");
                    if (paysTaxesNew.equals("yes") || paysTaxesNew.equals("no")){
                        menuItem.setPaysTaxes(paysTaxesNew);
                    }
                    else{
                        // Send error
                        request.setAttribute("error", "El campo de Impuestos es incorrecto");
                        viewToSend = adminMenuItemsRoute;
                        break;
                    }
                    
                    // Update in DB
                    boolean updated = menuItemDAO.updateMenuItem(menuItem);
                    
                    if (!updated){
                        request.setAttribute("error", "No se modificó el Item de Menu exitosamente");
                        viewToSend = adminMenuItemsRoute;
                        break;
                    }
                    else if (updated){
                        // Confirmation message
                        request.setAttribute("success", "Producto modificado exitosamente");
                        viewToSend = adminMenuItemsRoute;
                        break;
                    }
                    
                } catch (Exception e) {
                    System.out.println("Error when updating the Menu Item " + e);
                }
            break;
            case "adminMenuItems":
                viewToSend= adminMenuItemsRoute;
            break;
            case "adminMenu":
                viewToSend = adminMenuRoute;
            break;
            case "adminUsers":
                viewToSend = adminUsersRoute;
            break;
            case "goToUpdateMenuItemView":
                request.setAttribute("code", request.getParameter("code"));
                viewToSend = updatMenuItemViewRoute;
            break;
            case "goToUpdateUserView" :
                request.setAttribute("id", request.getParameter("id"));
                System.out.println("The id that comes from adminUsers and is going to be updated is: " + request.getParameter("id"));
                viewToSend = updateUserViewRoute;
            break;  
            
            case "updateUser":
                System.out.println("request: "+request.getParameterMap().toString());
                
                // Validate password fields
                String password = request.getParameter("password");
                String passwordRepeat = request.getParameter("passwordRepeat");
                
                if (!password.equals(passwordRepeat))
                {
                    // Send error
                    request.setAttribute("error", "Los campos de contraseña no son iguales");
                    viewToSend = adminUsersRoute;
                    break;
                }
                
                // Retrieve User from DB to edit with new data
                String oid = request.getParameter("oid");
                User user = userDAO.listUser(oid);
                // Verify that the fullName has not been changed
                String fullNameWeb = request.getParameter("fullName");
                String fullNameDB = user.getFullName();
                System.out.println("FullName Web: " + fullNameWeb);
                System.out.println("FullName DB: " + fullNameDB);
                if(fullNameWeb.equals(fullNameDB))
                {
                    System.out.println("Both fullNames are equal");
                    // Update user in DB
                    boolean updated = false;
                    try {
                        // Copy to a user
                        user.setOidString(request.getParameter("oid"));
                        user.setEmail(request.getParameter("email"));
                        user.setUsername(request.getParameter("username"));
                        String hashedPassword = userDAO.hashPassword(request.getParameter("password"));
                        user.setPasswordHash(hashedPassword);
                        
                        String userTypeEntered = request.getParameter("userType");
                        if (userTypeEntered.equals("cashier") || userTypeEntered.equals("admin"))
                        {
                            user.setType(request.getParameter("userType"));
                        }
                        else{
                            // Send error
                            request.setAttribute("error", "El tipo de Usuario es Incorrecto");
                            viewToSend = adminUsersRoute;
                            break;
                        }
                        
                        updated = userDAO.updateUser(user);
                        
                    } catch (Exception e) {
                        System.out.println("Error when Updating User in DB");
                    }
                    
                    if (!updated){
                        request.setAttribute("error", "No se modificó el usuario exitosamente");
                        viewToSend = adminUsersRoute;
                        break;
                    }
                    else if (updated){
                        // Confirmation message
                        request.setAttribute("success", "Usuario modificado exitosamente");
                        viewToSend = adminUsersRoute;
                        break;
                    }
                }
                // Else, the user changed the HTML
                else {
                    request.setAttribute("error", "El Nombre de Usuario no coincide con los Datos");
                    viewToSend = adminUsersRoute;
                    System.out.println("The Client changed the HTML, he is vivo");
                    break;
                }
                
            break;
            case "deleteUser":
                String oidToDelete = request.getParameter("id");
                boolean deleted = userDAO.deleteUser(oidToDelete);
                if(deleted){
                    request.setAttribute("success", "Usuario eliminado exitosamente");
                    viewToSend = adminUsersRoute;
                }else{
                    request.setAttribute("error", "No se pudo eliminar el Usuario");
                    viewToSend = adminUsersRoute;
                }
            break;
            case "goToCreateUserView" :
                viewToSend = createUserViewRoute;
            break;
            case "createUser":
                User newUser = new User();
                newUser.setFullName(request.getParameter("fullName"));
                newUser.setEmail(request.getParameter("email"));
                newUser.setPasswordHash(userDAO.hashPassword(request.getParameter("password")));
                newUser.setUsername(request.getParameter("username"));
                newUser.setType(request.getParameter("userType"));
                boolean userAdded = userDAO.addUser(newUser);
                if(userAdded){
                    request.setAttribute("success", "Usuario creado exitosamente");
                    viewToSend = adminUsersRoute;
                }else{
                    request.setAttribute("error", "No se pudo crear el Usuario");
                    viewToSend = adminUsersRoute;
                }
            break; 
            case "adminInvoices":
                viewToSend = adminInvoiceViewRoute;    
            break;
            case "createProduct":
                MenuItem newmenuItem = new MenuItem();
                newmenuItem.setStatus(request.getParameter("status"));
                newmenuItem.setCode(request.getParameter("code"));
                newmenuItem.setCategory(request.getParameter("category"));
                newmenuItem.setName(request.getParameter("name"));
                newmenuItem.setPrice(new BigDecimal(request.getParameter("price")));
                newmenuItem.setPaysTaxes(request.getParameter("paysTaxes"));
                
                boolean menuItemAdded = menuItemDAO.addMenuItem(newmenuItem);
                if(menuItemAdded){
                    request.setAttribute("success", "Producto creado exitosamente");
                    viewToSend = adminMenuItemsRoute;
                }else{
                    request.setAttribute("error", "No se pudo crear el Producto");
                    viewToSend = adminMenuItemsRoute;
                }
            break;
            case "deleteMenuItem":
                String codeToDelete = request.getParameter("code");
                boolean delete = menuItemDAO.deleteMenuItem(codeToDelete);
                if(delete){
                    request.setAttribute("success", "producto eliminado exitosamente");
                    viewToSend = adminMenuItemsRoute;
                }else{
                    request.setAttribute("error", "No se pudo eliminar el producto");
                    viewToSend = adminMenuItemsRoute;
                }
            break;
            
            case "goToUpdateInvoiceView" :
                String id = request.getParameter("id");
                request.setAttribute ("id", id);
                viewToSend = updateInvoiceViewRoute;
            break;
            case "adminClients":
                viewToSend = adminClientsViewRoute;
            break;
            case "goToUpdateClientView":
                viewToSend = updateClientViewRoute;
            break;
            case "updateClient":
                String idClientToUpdate = request.getParameter("id");
                try {
                    Client client = clientDAO.getClient(idClientToUpdate);
                    
                    // Correct the id
                    client.setId(new ObjectId(idClientToUpdate));
                    
                    //Update Client with form fields
                    client.setIdCard(request.getParameter("idCard"));
                    client.setName(request.getParameter("name"));
                    client.setAddress(request.getParameter("address"));
                    client.setCellphone(request.getParameter("cellphone"));
                    client.setEmail(request.getParameter("email"));
                    
                    
                   // Update in DB
                    boolean updated = clientDAO.updateClient(client);
                    
                    if (!updated){
                        request.setAttribute("error", "No se modificó el Cliente exitosamente");
                        viewToSend = adminClientsViewRoute;
                        break;
                    }
                    else if (updated){
                        // Confirmation message
                        request.setAttribute("success", "producto modificado exitosamente");
                        viewToSend = adminClientsViewRoute;
                        break;
                    }
                    
                } catch (Exception e) {
                    System.out.println("Could not update the client to be updated " + e);
                }
            break;
            case "goToCreateClientView":
                viewToSend = createClientViewRoute;
            break;
            case "createClient":
                Client client = new Client();
                client.setName(request.getParameter("name"));
                client.setIdCard(request.getParameter("idCard"));
                client.setAddress(request.getParameter("address"));
                client.setCellphone(request.getParameter("cellphone"));
                client.setEmail(request.getParameter("email"));
                boolean created = clientDAO.addClient(client);
                
                if (!created){
                        request.setAttribute("error", "No se modificó el Cliente exitosamente");
                        viewToSend = adminClientsViewRoute;
                        break;
                }
                else if (created){
                    // Confirmation message
                    request.setAttribute("success", "Cliente creado exitosamente");
                    viewToSend = adminClientsViewRoute;
                    break;
                }
            break;
            case "updateInvoice":
                viewToSend = adminInvoiceViewRoute;
            
            break;
            default:
                viewToSend = "";
            
        }
    
              
        
        
        System.out.println("View to Send is: " + viewToSend);
        // Code modified from Michael Cobacango's tutorial
        // https://www.youtube.com/playlist?list=PLLV74Oll0_H1NdtWeGRjBLWrAUjSYzKNw
        RequestDispatcher view = request.getRequestDispatcher(validateDispatcherPath(viewToSend));
        view.forward(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
