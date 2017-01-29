package anaphora.helper;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import anaphora.domain.AnaphorInfo;
import edu.stanford.nlp.process.DocumentPreprocessor;

/**
 * Created by Mykhailo Yashchuk on 24.01.2017.
 */
public class AnaphorIterable implements Iterable<AnaphorInfo> {
    private Reader textReader;

    public AnaphorIterable(Reader textReader) {
        this.textReader = textReader;
    }

    @Override
    public Iterator<AnaphorInfo> iterator() {
        return new AnaphorIterator(new DocumentPreprocessor(textReader).iterator());
    }
}
