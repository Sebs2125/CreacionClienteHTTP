package org.example;

import io.javalin.Javalin;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main
{
    public static void main( String[] args )
    {
        Scanner escaner =  new Scanner( System.in );

        System.out.println("Validador HTTP");
        System.out.println("Ingrese una URL valida: " );
        String url = escaner.nextLine();

        try {
            validarHTTP( url );
        }catch (IOException e){
            System.err.println("Error al compilar la URL: " + e.getMessage() );
        } catch ( Exception e){
            System.err.println("Error de manera general. " + e.getMessage() );
            e.printStackTrace();
        }

        escaner.close();

    }

    private static void validarHTTP(String url) throws Exception //Funcion parte A de la practica
    {
        try ( CloseableHttpClient httpclient = HttpClients.createDefault() )
        {
            HttpGet httpget = new HttpGet( url );

            httpclient.execute( httpget, response -> {

                String tipoDeContenido = "desconocido";

                if ( response.getFirstHeader("Content-Type") != null )
                {
                    tipoDeContenido = response.getFirstHeader("Content-Type").getValue();
                }

                System.out.println("\na) Tipo de recurso: " +tipoDeContenido);

                HttpEntity entity = response.getEntity();

                if ( entity != null )
                {

                    String contenido = EntityUtils.toString(response.getEntity());

                    if (tipoDeContenido.contains("text/html")) {
                        verificadorHTML(contenido, url);
                    } else {
                        System.out.println("El recurso no es de tipo HTML, por ello no se puede verificar este tipo de URL o direccion no valida.");
                    }

                    EntityUtils.consume(entity);

                }

                return null;

            });

        }
    }

    private static void verificadorHTML( String html, String url ) //Parte B
    {
        Document documento = Jsoup.parse(html);

        System.out.println("\nb) Es un documento HTML: ");

        //Punto #1
        int lineas = html.split("\n").length;
        System.out.println("1- Cantidad de lineas: " +lineas );

        //Punto #2
        Elements parrafos = documento.select("p");
        System.out.println("2- Cantidad de parrafos: " +parrafos.size() );

        //Punto #3
        int imagenesEnParrafos = 0;

        for ( Element p : parrafos )
        {
            imagenesEnParrafos += p.select("img").size();
        }

        System.out.println("3- Cantidad de imagenes en los parrafos: " + imagenesEnParrafos );

        //Punto #4
        System.out.println("4- Cantidad de formularios en el documento y específicar los GET y POST: ");
        Elements forms = documento.select("form");
        int formPost = 0;
        int formGet = 0;

        for ( Element form : forms )
        {
            String metodo = form.attr("method").toUpperCase();

            if ( metodo.isEmpty() )
            {
                formGet++;
            }
            else if ( metodo.equals("GET") )
            {
                formGet++;
            }
            else if ( metodo.equals("POST") )
            {
                formPost++;
            }

        }

        System.out.println("Cantidad de Forms: " + forms.size() );
        System.out.println("Cantidad de Form tipo Post: " +  formPost );
        System.out.println("Cantidad de Form tipo Get: " +  formGet );

        //Punto #5
        int formNumero = 1;
        for ( Element form : forms )
        {
            Elements inputs = form.select("input");
            System.out.println("\n 5- Campos de input y su respectivo tipo en el documento:");
            System.out.println("Formulario: " + formNumero + ":" );

            if ( inputs.isEmpty() )
            {
                System.out.println("No posee inputs");
            }

            for ( Element input : inputs )
            {
                String tipo = input.attr("type");
                String nombre = input.attr("name");

                if ( tipo.isEmpty() )
                {
                    tipo = "text";
                }

                System.out.println(" -Input: name=' " + nombre + " ', tipo=' " + tipo + " " );

            }

            formNumero++;

        }

        //Punto #6
        System.out.println("\n 6- Para cada formulario parseado identificar que el envio sea POST, peticion al servidor con parametro asignatura, valor practica1 y heather matricula-id: ");

        formNumero = 1;

        for ( Element form : forms )
        {
            String metodo = form.attr("method").toUpperCase();

            if ( metodo.equals("POST") )
            {
                System.out.println(" Formulario " + formNumero + "(POST):" );

                String formAction = form.attr("action");
                String formUrl = formAction;

                if (!formAction.isEmpty() && !formAction.startsWith("http") )
                {
                    if ( formAction.startsWith("?") )
                    {
                        formUrl = url + formAction;
                    }
                    else
                    {
                        formUrl = url.replaceAll("\\?.*", "") + "/" + formAction;
                    }
                }
                else if ( formAction.isEmpty() )
                {
                    formUrl = url;
                }

                System.out.println(" - Action Url: " + formUrl );
                System.out.println(" - Metodo: POST");

                try ( CloseableHttpClient httpClient = HttpClients.createDefault() )
                {
                    HttpPost httpPost = new HttpPost( formUrl );

                    httpPost.setHeader("matricula-id", "2023-0907");

                    List<NameValuePair> parametros = new ArrayList<>();
                    parametros.add( new BasicNameValuePair("asignatura", "practica1" ) );
                    httpPost.setEntity(new UrlEncodedFormEntity(parametros));

                    httpClient.execute(httpPost, response -> {
                        System.out.println(" - Respuesta del server: " + response.getCode() );
                        System.out.println(" - Parametro enviado: asignatura=practica1" );
                        System.out.println(" - Header enviado: matricula-id=2023-0907" );
                        return null;
                    });

                }catch (Exception e)
                {
                    System.out.println("- Error referido a la peticion: " + e.getMessage() );
                }

            }

            formNumero++;

        }

    }




}
