package com.example.nathanphan.googlemapstest;

/**
 * Created by Nathan Phan on 6/9/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by Jun Jie Mo on 5/23/17.
 */

/*
    Zooming in/out of image code was taken from Stack overflow
    https://stackoverflow.com/questions/6650398/android-imageview-zoom-in-and-zoom-out
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {


    private Context context;
    private List<Upload> uploads;

    public MyAdapter(Context context, List<Upload> uploads) {
        this.uploads = uploads;
        this.context = context;
    }

    private void dumpEvent(MotionEvent event) {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }
        sb.append("]");

    }

    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_images, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;


    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Upload upload = uploads.get(position);

        holder.textViewName.setText(upload.getName());

        Glide.with(context).load(upload.getUrl()).into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return uploads.size();
    }



    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        ImageView imageView;


        private static final String TAG = "Touch";
        @SuppressWarnings("unused")
        private static final float MIN_ZOOM = 1f,MAX_ZOOM = 1f;
        // These matrices will be used to scale points of the image
        Matrix matrix = new Matrix();
        Matrix savedMatrix = new Matrix();

        // The 3 states (events) which the user is trying to perform
        static final int NONE = 0;
        static final int DRAG = 1;
        static final int ZOOM = 2;
        int mode = NONE;

        // these PointF objects are used to record the point(s) the user is touching
        PointF start = new PointF();
        PointF mid = new PointF();
        float oldDist = 1f;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);

            imageView.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    ImageView view = (ImageView) v;
                    view.setScaleType(ImageView.ScaleType.MATRIX);
                    float scale;

                    dumpEvent(event);
                    // Handle touch events here...

                    switch (event.getAction() & MotionEvent.ACTION_MASK)
                    {
                        case MotionEvent.ACTION_DOWN:   // first finger down only
                            savedMatrix.set(matrix);
                            start.set(event.getX(), event.getY());
                            Log.d(TAG, "mode=DRAG"); // write to LogCat
                            mode = DRAG;
                            break;

                        // first finger lifted
                        case MotionEvent.ACTION_UP:

                            // second finger lifted
                        case MotionEvent.ACTION_POINTER_UP:

                            mode = NONE;
                            Log.d(TAG, "mode=NONE");
                            break;

                        case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                            oldDist = spacing(event);
                            Log.d(TAG, "oldDist=" + oldDist);
                            if (oldDist > 5f) {
                                savedMatrix.set(matrix);
                                midPoint(mid, event);
                                mode = ZOOM;
                                Log.d(TAG, "mode=ZOOM");
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:

                            if (mode == DRAG)
                            {
                                matrix.set(savedMatrix);
                                matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                            }
                            else if (mode == ZOOM)
                            {
                                // pinch zooming
                                float newDist = spacing(event);
                                Log.d(TAG, "newDist=" + newDist);
                                if (newDist > 5f)
                                {
                                    matrix.set(savedMatrix);
                                    scale = newDist / oldDist; // setting the scaling of the
                                    // matrix...if scale > 1 means
                                    // zoom in...if scale < 1 means
                                    // zoom out
                                    matrix.postScale(scale, scale, mid.x, mid.y);
                                }
                            }
                            break;
                    }

                    view.setImageMatrix(matrix);
                    return true;
                }
            });
        }
    }


}