// Author: Jose Ignacio Yanez
// Based on: https://www.youtube.com/watch?v=nI8PYZNFtac & for React18 error of useEffect https://www.youtube.com/watch?v=81faZzp18NM
import { useState, useEffect, useRef } from 'react';
import useAxiosPrivate from '../hooks/useAxiosPrivate';
import Grid from '@mui/material/Grid';
import Button from '@mui/material/Button';
import { DataGrid } from '@mui/x-data-grid';
import { useNavigate } from 'react-router-dom';


const AdminClients = () => {
    const [ clients, setClients ] = useState();
    const axiosPrivate = useAxiosPrivate();
    // For react18 changed useEffect to run Twice
    const effectRan = useRef(false)

    const [isLoading, setLoading ] = useState(true);
    const [ isDeleting, setDeleting ] = useState(false);

    const navigate = useNavigate();

    // Get data
    useEffect(() => {
    
        if (effectRan.current === false){

            const getClients = async () => {
                try {
                    const response = await axiosPrivate.get('/restaurant/clients', {
                    });
                    console.log(response.data)
                    // If Component isMounted, set data and map it for rows DataGrid
                    setClients(response.data.map(client => (
                        {
                            id: Math.random() * (100000 - 1) + 1,
                            idCard: client.idCard,
                            name: client.name,
                            address: client.address,
                            cellphone: client.cellphone,
                            email: client.email
                        }
                    )));
                    
                    setLoading(false);
                } catch (error) {
                    console.log(error);
                    // Handle the case the refreshToken of the DB expires, make Login but return to where user was
                    // Even though this is crashing because the first requests are getting canceled TODO
                    //navigate('/login', { state: { from: location }, replace: true });
                }
            }

            getClients();
            // Cleanup function
            return () => {
                effectRan.current = true;
            }
        }
        
    }, [isDeleting])

    // DataGrid
    const ModifyButton = (params) => {
        const navigate = useNavigate();  

        function handleModify() {
            navigate("/modifyClient", {state:{idCard:params.row.idCard}})
        }

        return (
            <Button onClick={ handleModify } sx={{background:'#0087BD', color:"#fff", "&:hover": {color: '#fff', background: '#1F75FE'}, borderRadius: '0.5rem'}}>Modificar</Button>
        );
    };
    const DeleteButton = (params) => {

        async function handleDelete() {
            //Get username to delete
            setDeleting(true)

            const idCardDelete = params.row.idCard;

            const deleteClient = async () => {
                try {
                    await axiosPrivate.delete(`/restaurant/client/${idCardDelete}`);
                } catch (error) {
                    console.log(error);
                } finally {
                    // Wait
                    await new Promise(r => setTimeout(r, 1500));
                    setDeleting(false)
                    effectRan.current = false;
                }
            }
            deleteClient();
        }

        return (
            <Button onClick={handleDelete} sx={{background:'#8C1127', color:"#fff", "&:hover": {color: '#fff', background: '#DA2C43'}, borderRadius: '0.5rem'}}>Borrar</Button>
        );
    };
    const ReportButton = (params) => {

        async function handleReport() {
            const idCardReport= params.row.idCard;

            const reportClient = async () => {
                try {
                    const response = await axiosPrivate.get(`/restaurant/client/${idCardReport}/report`);
                    alert(response.data.message)
                } catch (error) {
                    console.log(error);
                } finally {
                    // Wait
                    await new Promise(r => setTimeout(r, 1500));
                    
                }
            }
            reportClient();
        }

        return (
            <Button onClick={handleReport} sx={{background:'#7851A9', color:"#fff", "&:hover": {color: '#fff', background: '#880085'}, borderRadius: '0.5rem'}}>Reporte</Button>
        );
    };
    
    const columns = [
        { field: 'id', hide: true },
        { field:'idCard', headerName:'Cédula', width: 170 },
        { field:'name', headerName:'Nombre', width: 200 },
        { field:'address', headerName:'Dirección', width: 300 },
        { field:'cellphone', headerName:'Teléfono', width: 200 },
        { field:'email', headerName:'Mail', width: 300 },
        { field:'modifyButton', headerName:'Modificar', width:150, renderCell: ModifyButton },
        { field:'deleteButton', headerName:'Borrar', width:120, renderCell:DeleteButton },
        { field:'reportButton', headerName:'Reporte', width:150, renderCell:ReportButton }
    ];

    if (isLoading) {
        return <><h1>Cargando...</h1></>
    }

    if (isDeleting) {
        return <><h1>Borrando...</h1></>
    }

    return (
        <>
            <h1>Clientes</h1>
            <br/>
            <Button onClick={() => navigate("/newClient")} sx={{background:'#009F6B', color:"#fff", "&:hover": {color: '#fff', background: '#32CD32'}, borderRadius: '0.5rem'}}>Crear Nuevo Cliente</Button>
            <br/>
            <Grid container justifyContent="center" sx={{ height: 500, width: '72%'}}> 
                <DataGrid columns={columns} rows={clients} className="dataGrid" sx={{alignSelf:"center"}}/>
            </Grid>
            <br/>
            <Button onClick={() => navigate("/admin")} sx={{background:'rgb(144,30,56)', color:"#fff", "&:hover": {color: '#fff', background: '#DA2C43'}, borderRadius: '0.5rem'}}>Volver</Button>
        </>
    );
}

export default AdminClients;
