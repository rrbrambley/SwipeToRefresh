package net.callumtaylor.swipetorefresh.helper;

import net.callumtaylor.pulltorefresh.R;
import net.callumtaylor.swipetorefresh.view.OnOverScrollListener;
import net.callumtaylor.swipetorefresh.view.RefreshableListView;
import net.callumtaylor.swipetorefresh.view.RefreshableScrollView;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RefreshHelper implements OnOverScrollListener
{
	private final View ptrOverlay;

	private final ProgressBar ptrProgressBar, ptrInderterminateProgressBar;
	private final AccelerateInterpolator accelerationInterpolator;
	private final View abRoot;
	private OnRefreshListener refreshListener;

	private RefreshableListView listView;
	private RefreshableScrollView scrollView;

	private boolean refreshing = false;

	private RefreshHelper(View overlay, View progressOverlay, View root)
	{
		this.ptrOverlay = overlay;
		this.ptrInderterminateProgressBar = (ProgressBar)progressOverlay;
		this.accelerationInterpolator = new AccelerateInterpolator();
		this.ptrProgressBar = (ProgressBar)overlay.findViewById(R.id.refresh_progress);
		this.abRoot = root;

		ptrProgressBar.setMax(0);
		ptrProgressBar.setMax(100);
		ptrProgressBar.setProgress(0);
	}

	public void hideHelper()
	{
		if (isRefreshing())
		{
			ptrInderterminateProgressBar.setVisibility(View.GONE);
		}
	}

	public boolean isRefreshing()
	{
		return refreshing;
	}

	@Override public void onBeginRefresh()
	{
		onRefreshScrolledPercentage(1.0f);
		AnimationHelper.pullRefreshActionBar(ptrOverlay, abRoot);
	}

	@Override public void onRefresh()
	{
		refreshing = true;
		ptrProgressBar.setVisibility(View.GONE);
		ptrInderterminateProgressBar.setVisibility(View.VISIBLE);
		((TextView)ptrOverlay.findViewById(R.id.refresh_text)).setText(R.string.ptr_refreshing);

		ptrOverlay.postDelayed(new Runnable()
		{
			@Override public void run()
			{
				resetOverlay();
			}
		}, 800);

		if (refreshListener != null)
		{
			refreshListener.onRefresh();
		}
	}

	@Override public void onRefreshScrolledPercentage(float percentage)
	{
		ptrProgressBar.setVisibility(View.VISIBLE);
		ptrProgressBar.setProgress(Math.round(accelerationInterpolator.getInterpolation(percentage) * 100));
	}

	@Override public void onReset()
	{
		refreshing = false;
		if (ptrInderterminateProgressBar.getVisibility() == View.VISIBLE)
		{
			AnimationHelper.fadeOut(ptrInderterminateProgressBar);
		}

		resetOverlay();
	}

	private void resetOverlay()
	{
		if (ptrOverlay.getVisibility() == View.VISIBLE)
		{
			AnimationHelper.pullRefreshActionBarCancel(ptrOverlay, abRoot);
			ptrProgressBar.setVisibility(View.GONE);
			ptrProgressBar.setProgress(0);

			ptrOverlay.postDelayed(new Runnable()
			{
				@Override public void run()
				{
					((TextView)ptrOverlay.findViewById(R.id.refresh_text)).setText(R.string.ptr_pull);
				}
			}, 400);
		}
		else
		{
			((TextView)ptrOverlay.findViewById(R.id.refresh_text)).setText(R.string.ptr_pull);
		}
	}

	public void setOnRefreshListener(OnRefreshListener l)
	{
		this.refreshListener = l;
	}

	public void setRefreshableListView(RefreshableListView l)
	{
		this.listView = l;
		this.listView.setOnOverScrollListener(this);
	}

	public void setRefreshableScrollView(RefreshableScrollView l)
	{
		this.scrollView = l;
		this.scrollView.setOnOverScrollListener(this);
	}

	public void setRefreshing(boolean refreshing)
	{
		this.refreshing = refreshing;
	}

	public void showHelper()
	{
		if (isRefreshing())
		{
			ptrInderterminateProgressBar.setVisibility(View.VISIBLE);
		}
	}

	public static View findActionBar(Window w)
	{
		return getFirstChildByClassName((ViewGroup)w.getDecorView(), "com.android.internal.widget.ActionBarContainer");
	}

	public static View getFirstChildByClassName(ViewGroup parent, String name)
	{
		View retView = null;
		int childCount = parent.getChildCount();

		for (int childIndex = 0; childIndex < childCount; childIndex++)
		{
			View child = parent.getChildAt(childIndex);

			if (child.getClass().getName().equals(name))
			{
				return child;
			}

			if (child instanceof ViewGroup)
			{
				View v = getFirstChildByClassName((ViewGroup)child, name);

				if (v != null)
				{
					return v;
				}
			}
		}

		return retView;
	}

	public static RefreshHelper wrapRefreshable(Activity ctx, RefreshableListView list, OnRefreshListener l)
	{
		ViewGroup abRoot = null;

		int id = ctx.getResources().getIdentifier("abs__action_bar_container", "id", ctx.getPackageName());

		if (id > 0)
		{
			abRoot = (ViewGroup)ctx.getWindow().getDecorView().findViewById(id);
		}

		if (id == 0 || abRoot == null)
		{
			abRoot = (ViewGroup)findActionBar(ctx.getWindow());
		}

		if (abRoot != null)
		{
			View overlay = LayoutInflater.from(ctx).inflate(R.layout.abs_overlay, abRoot, false);
			abRoot.addView(overlay);

			View progressOverlay = LayoutInflater.from(ctx).inflate(R.layout.abs_overlay_progress, abRoot, false);
			abRoot.addView(progressOverlay);

			RefreshHelper helper = new RefreshHelper(overlay, progressOverlay, abRoot.getChildAt(0));
			helper.setOnRefreshListener(l);
			helper.setRefreshableListView(list);
			return helper;
		}

		return null;
	}

	public static RefreshHelper wrapRefreshable(Activity ctx, RefreshableScrollView list, OnRefreshListener l)
	{
		ViewGroup abRoot = null;

		int id = ctx.getResources().getIdentifier("abs__action_bar_container", "id", ctx.getPackageName());

		if (id > 0)
		{
			abRoot = (ViewGroup)ctx.getWindow().getDecorView().findViewById(id);
		}

		if (id == 0 || abRoot == null)
		{
			abRoot = (ViewGroup)findActionBar(ctx.getWindow());
		}

		if (abRoot != null)
		{
			View overlay = LayoutInflater.from(ctx).inflate(R.layout.abs_overlay, abRoot, false);
			abRoot.addView(overlay);

			View progressOverlay = LayoutInflater.from(ctx).inflate(R.layout.abs_overlay_progress, abRoot, false);
			abRoot.addView(progressOverlay);

			RefreshHelper helper = new RefreshHelper(overlay, progressOverlay, abRoot.getChildAt(0));
			helper.setOnRefreshListener(l);
			helper.setRefreshableScrollView(list);
			return helper;
		}

		return null;
	}

	public interface OnRefreshListener
	{
		public void onRefresh();
	}
}