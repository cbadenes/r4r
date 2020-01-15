package es.upm.oeg.r4r.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class QueryView {

    private static final Logger LOG = LoggerFactory.getLogger(QueryView.class);

    private final String raw;

    public QueryView(String raw) {
        this.raw = raw.replaceAll("\n"," ");
    }

    public List<String> getSelectFields(){

        List<String> fields = new ArrayList<>();

        Matcher matcher = Pattern.compile("SELECT(.*)WHERE", Pattern.MULTILINE).matcher(raw);

        if (matcher.find()){
            String g1 = matcher.group(1).replaceAll(" ","").replaceAll("\\?"," ").trim();
            fields = Arrays.stream(g1.split(" ")).collect(Collectors.toList());
        }

        return fields;
    }


}
