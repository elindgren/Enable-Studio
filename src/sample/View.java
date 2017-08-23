package sample;

/**
 * Created by Eric on 2017-07-14.
 */
public interface View {

    //**** Interaction ****//
    public void setupButtons();
    public void setupSlider();

    //**** Animations ****//
    public void prepareSlideButtonAnimation();

    //**** Math ****//
    public void findMin();
    public void findMax();

    //**** Chip status ****//
    public void setupStatusListener();

    //**** Graph & Table ****//
    public void initStaticGraph();
    public void initAnimatedGraph();
    public void resetGraph();
    public void setupTable();

    //**** Animated & cinematic mode ****//
    public void timelineAnimated(String str);
    public void startAnimatedTimeline(String str);
    public void stopAnimatedTimeline();
    public void readFileCinematic();
    public void readChipCinematic();
}
