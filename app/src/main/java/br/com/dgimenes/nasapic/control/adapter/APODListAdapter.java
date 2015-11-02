package br.com.dgimenes.nasapic.control.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.List;

import br.com.dgimenes.nasapic.R;
import br.com.dgimenes.nasapic.control.activity.DetailActivity;
import br.com.dgimenes.nasapic.model.APOD;
import br.com.dgimenes.nasapic.service.DefaultPicasso;
import br.com.dgimenes.nasapic.util.DateUtils;
import br.com.dgimenes.nasapic.util.StringUtils;
import butterknife.Bind;
import butterknife.ButterKnife;

public class APODListAdapter extends RecyclerView.Adapter<APODListAdapter.ViewHolder> {

    private final Picasso picasso;
    private List<APOD> dataset;
    private WeakReference<Context> contextWeak;

    private APODListAdapter.ErrorListener errorListener;
    private int listWidth;

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.date)
        public TextView dateTextView;

        @Bind(R.id.title)
        public TextView titleTextView;

        @Bind(R.id.explanation)
        public TextView explanationTextView;

        @Bind(R.id.apod_preview_image)
        public ImageView apodPreviewImageView;

        @Bind(R.id.loading_indicator)
        public ProgressBar loadingIndicator;

        public ViewHolder(View cardView) {
            super(cardView);
            ButterKnife.bind(this, cardView);
        }
    }

    public APODListAdapter(Context context, List<APOD> dataset, ErrorListener errorListener,
                           int listWidth) {
        this.dataset = dataset;
        this.errorListener = errorListener;
        this.listWidth = listWidth;
        this.picasso = DefaultPicasso.get(context, new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                displayErrorMessage(R.string.error_downloading_apod);
            }
        });
        this.contextWeak = new WeakReference<>(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View apodCardRootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.apod_card,
                parent, false);
        return new ViewHolder(apodCardRootView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.loadingIndicator.setVisibility(View.VISIBLE);
        APOD apod = dataset.get(position);
        viewHolder.dateTextView.setText(
                DateUtils.friendlyDateString(contextWeak.get(), apod.getDate()));
        viewHolder.titleTextView.setText(apod.getTitle());
        viewHolder.explanationTextView.setText(
                StringUtils.addQuotes(apod.getExplanation()));
        viewHolder.apodPreviewImageView.setTag(apod.getUrl());
        viewHolder.itemView.setOnClickListener(new OnCardClickListener(contextWeak.get(), apod));
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder viewHolder) {
        String src = (String) viewHolder.apodPreviewImageView.getTag();
        picasso.load(src).resize(listWidth, 0).into(viewHolder.apodPreviewImageView,
                new AfterLoadingImageCallback(viewHolder.loadingIndicator));
    }

    private void displayErrorMessage(int errorMessageResource) {
        if (errorListener != null) {
            String errorMessage = contextWeak.get().getResources().getString(errorMessageResource);
            errorListener.error(errorMessage);
        }
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public interface ErrorListener {
        void error(String errorMessage);
    }

    private class AfterLoadingImageCallback implements Callback {
        private ProgressBar loadingIndicator;

        public AfterLoadingImageCallback(ProgressBar loadingIndicator) {
            this.loadingIndicator = loadingIndicator;
        }

        @Override
        public void onSuccess() {
            loadingIndicator.setVisibility(View.GONE);
        }

        @Override
        public void onError() {
        }
    }

    private class OnCardClickListener implements View.OnClickListener {
        private APOD apod;
        private WeakReference<Context> contextWeak;

        public OnCardClickListener(Context context, APOD apod) {
            this.apod = apod;
            this.contextWeak = new WeakReference<>(context);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(contextWeak.get(), DetailActivity.class);
            intent.putExtra(DetailActivity.APOD_OBJECT_PARAM, apod);
            contextWeak.get().startActivity(intent);
        }
    }
}
