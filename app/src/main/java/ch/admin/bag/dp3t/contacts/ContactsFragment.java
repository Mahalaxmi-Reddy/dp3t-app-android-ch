/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.contacts;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.main.TracingBoxFragment;
import ch.admin.bag.dp3t.main.views.HeaderView;
import ch.admin.bag.dp3t.viewmodel.TracingViewModel;

public class ContactsFragment extends Fragment {

	private static final int REQUEST_CODE_BLE_INTENT = 330;

	private TracingViewModel tracingViewModel;
	private HeaderView headerView;
	private ScrollView scrollView;

	private View tracingStatusView;
	private View tracingErrorView;
	private Switch tracingSwitch;

	public static ContactsFragment newInstance() {
		return new ContactsFragment();
	}

	public ContactsFragment() { super(R.layout.fragment_contacts); }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
		getChildFragmentManager()
				.beginTransaction()
				.add(R.id.status_container, TracingBoxFragment.newInstance(false))
				.commit();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.contacts_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		tracingStatusView = view.findViewById(R.id.tracing_status);
		tracingErrorView = view.findViewById(R.id.tracing_error);
		tracingSwitch = view.findViewById(R.id.contacts_tracing_switch);

		headerView = view.findViewById(R.id.contacts_header_view);
		scrollView = view.findViewById(R.id.contacts_scroll_view);
		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), tracingStatus -> {
			headerView.setState(tracingStatus);
		});
		setupScrollBehavior();
		setupTracingView();

		view.findViewById(R.id.contacts_faq_button).setOnClickListener(v -> {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.faq_button_url)));
			startActivity(browserIntent);
		});
	}

	private void setupTracingView() {
		Activity activity = requireActivity();

		tracingSwitch.setOnClickListener(v -> {
			if (tracingSwitch.isChecked()) {
				tracingViewModel.enableTracing(activity,
						() -> {
							// success, do nothing
						},
						(e) -> {
							new AlertDialog.Builder(activity, R.style.NextStep_AlertDialogStyle)
									.setMessage(e.getLocalizedMessage())
									.setPositiveButton(R.string.android_button_ok, (dialog, which) -> {})
									.show();
							tracingSwitch.setChecked(false);
						},
						() -> tracingSwitch.setChecked(false));
			} else {
				tracingViewModel.disableTracing();
			}
		});

		tracingViewModel.getTracingStatusLiveData().observe(getViewLifecycleOwner(), status -> {
			tracingSwitch.setChecked(status.isTracingEnabled());
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		headerView.stopAnimation();
	}

	private void setupScrollBehavior() {

		int scrollRangePx = getResources().getDimensionPixelSize(R.dimen.top_item_padding);
		int translationRangePx = -getResources().getDimensionPixelSize(R.dimen.spacing_huge);
		scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
			float progress = computeScrollAnimProgress(scrollY, scrollRangePx);
			headerView.setAlpha(1 - progress);
			headerView.setTranslationY(progress * translationRangePx);
		});
		scrollView.post(() -> {
			float progress = computeScrollAnimProgress(scrollView.getScrollY(), scrollRangePx);
			headerView.setAlpha(1 - progress);
			headerView.setTranslationY(progress * translationRangePx);
		});
	}

	private float computeScrollAnimProgress(int scrollY, int scrollRange) {
		return Math.min(scrollY, scrollRange) / (float) scrollRange;
	}

}
