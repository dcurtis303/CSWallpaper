package ca.dcstudios.cswallpaper.renderer;

import java.util.ArrayList;

public class AnimatedMeshes extends ArrayList<AnimatedMesh> {
    public void setVisible(int index) {
        for (int i = 0; i < size(); i++)
            get(i).setVisible(i == index);
    }
}
