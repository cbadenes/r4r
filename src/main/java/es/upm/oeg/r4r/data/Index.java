package es.upm.oeg.r4r.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Index {

    private static final Logger LOG = LoggerFactory.getLogger(Index.class);

    private String text;

    private Double value;

    public Index() {
    }

    public Index(String text, Double value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}

