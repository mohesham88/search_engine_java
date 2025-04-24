package InvertedIndex;

public class Posting {
    public final int docId;
    public int termFrequency;

    public Posting(int docId) {
        this.docId = docId;
        this.termFrequency = 1;
    }

    public void increment() {
        this.termFrequency++;
    }
}