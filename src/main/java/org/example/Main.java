package org.example;

import io.javalin.Javalin;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
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
        }

        escaner.close();

    }

    private static void validarHTTP(String url) throws Exception //Funcion parte A de la practica
    {
        try ( CloseableHttpClient httpclient = HttpClients.createDefault() )
        {
            HttpGet httpget = new HttpGet( url );

            ClassicHttpResponse response = httpclient.execute( httpget, res -> {
                return res;
            });

            String TipoDeContenido = response.getFirstHeader("Content-Type").getValue();
            System.out.println("\na) Tipo de recurso: " + TipoDeContenido );

            String contenido = EntityUtils.toString( response.getEntity() );

            if ( TipoDeContenido.contains("text/html") )
            {
                verificadorHTML( contenido, url );
            }
            else
            {
                System.out.println("El recurso no es de tipo HTML, por ello no se puede verificar este tipo de URL o direccion no valida.");
            }

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

    }

}
