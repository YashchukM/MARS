package anaphora.helper;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import org.junit.Test;

import anaphora.domain.AnaphorInfo;

/**
 * Created by Mykhailo Yashchuk on 27.01.2017.
 */
public class AnaphorIterableTest {
    @Test
    public void testIterator() throws IOException {
        String sentence = "Insert the cassette into the VCR, making sure it is suitable for the length of recording";
        try (Reader reader = new StringReader(sentence)) {
            AnaphorIterable ai = new AnaphorIterable(reader);
            Iterator<AnaphorInfo> iterator = ai.iterator();

            AnaphorInfo anaphorInfo = iterator.next();

            assertEquals(2, anaphorInfo.getCandidates().size());
            assertEquals(1, anaphorInfo.getCandidateSentences().size());
        }
    }

    @Test
    public void testIteratorWithThreeSentences() throws IOException {
        String sentence = "Insert the cassette into the VCR, making sure it is suitable for the length of recording. " +
                "Insert the cassette into the VCR, making sure it is suitable for the length of recording. " +
                "Insert the cassette into the VCR, making sure it is suitable for the length of recording.";
        try (Reader reader = new StringReader(sentence)) {
            AnaphorIterable ai = new AnaphorIterable(reader);
            Iterator<AnaphorInfo> iterator = ai.iterator();

            AnaphorInfo anaphorInfo = iterator.next();
            while (iterator.hasNext()) {
                anaphorInfo = iterator.next();
            }

            assertEquals(10, anaphorInfo.getCandidates().size());
            assertEquals(3, anaphorInfo.getCandidateSentences().size());
        }
    }
}
