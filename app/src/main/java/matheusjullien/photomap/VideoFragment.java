package matheusjullien.photomap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_video, container, false);

        VideoView video = (VideoView) rootView.findViewById(R.id.video);

        video.setVideoPath(getArguments().getString("path").replace("file:",""));
        video.setMediaController(new MediaController(getContext()));
        video.requestFocus();

        return rootView;
    }

    public static VideoFragment newInstance(Page page) {
        VideoFragment video = new VideoFragment();
        Bundle bundle = new Bundle();
        bundle.putString("path",page.getPath());

        video.setArguments(bundle);

        return video;
    }
}
