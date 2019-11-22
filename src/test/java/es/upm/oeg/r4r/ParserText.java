package es.upm.oeg.r4r;

import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class ParserText {

    private static final Logger LOG = LoggerFactory.getLogger(ParserText.class);


    @Test
    public void parser() throws FileNotFoundException, UnsupportedEncodingException {

        File file = new File("/Users/cbadenes/Downloads/out.json");
        Scanner sc = new Scanner(file);

        // we just need to use \\Z as delimiter
        sc.useDelimiter("\\Z");

        String txt = sc.next();

        System.out.println(txt);

        String cleanedTxt = txt.replaceAll("\\P{Print}", "");

        System.out.println("------------------");

        System.out.println(cleanedTxt);

    }

    private String parse(String txt){
        String t0 = txt.replaceAll("\\P{Print}", "");
        String t1 = StringEscapeUtils.unescapeHtml4(t0);
        String t2 = StringEscapeUtils.unescapeXml(t1);
        return t2;
    }

    @Test
    public void htmlCOde (){

        System.out.println(parse("According to government&amp;#039;s Sporting Future strategy published in December 2015, &amp;#039;economic development&amp;#039; represents one of the five outcomes we want to see from investment in sport and physical activity. As part of measuring this outcome, the Parties (DCMS, Sport England and UK Sport) are looking for a supplier to develop a new methodology for creating ratios to be applied to economic sectors to ascertain the sport-related element of each. The ultimate output would be the application of these ratios to figures for Gross Value Added (GVA), Employment, Trade (imports and exports of services) and the number of businesses that can be reliably compared with data produced in subsequent years and therefore create an annual UK Sport Satellite Account (SSA), taking the &amp;#039;Vilnius Definition&amp;#039; of the &amp;#039;sports economy&amp;#039; as a basis."));

        System.out.println(parse("Procesleider Mobiliteitsplan Delft 2040 "));


        System.out.println(parse("http://dbpedia.org/ontology/MusicalArtist"));



    }
}
