package matheusjullien.photomap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PhotoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo, container, false);

        ImageView photo = (ImageView) rootView.findViewById(R.id.photo);

        Bitmap mBitmap = BitmapFactory.decodeFile(getArguments().getString("path").replace("file:","").replace("%20"," ").replace("%3A",":"));
        photo.setImageBitmap(mBitmap);

        return rootView;
    }

    public static PhotoFragment newInstance(Page page) {
        PhotoFragment photo = new PhotoFragment();
        Bundle bundle = new Bundle();
        bundle.putString("path",page.getPath());

        photo.setArguments(bundle);

        return photo;
    }
}
