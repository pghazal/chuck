/*
 * Copyright (C) 2017 Jeff Gilfelt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.readystatesoftware.chuck.internal.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.*;
import com.readystatesoftware.chuck.R;
import com.readystatesoftware.chuck.internal.data.ChuckContentProvider;
import com.readystatesoftware.chuck.internal.data.HttpTransaction;
import com.readystatesoftware.chuck.internal.support.*;

import java.util.ArrayList;

public class TransactionListFragment extends Fragment implements
        SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static int ACTIVITY_REQUEST_CODE_SETTINGS = 1234;

    private String currentFilter;
    private OnListFragmentInteractionListener listener;
    private TransactionAdapter adapter;

    private SettingsManager settingsManager;

    private AppExecutors appExecutors;
    private ExportUtils exportUtils;
    private String mSelection;
    private String[] mSelectionArgs;
    private String[] mProjection;
    private String mSortOrder;

    public TransactionListFragment() {
    }

    public static TransactionListFragment newInstance() {
        return new TransactionListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        settingsManager = new SettingsManager(getContext());
        appExecutors = new AppExecutors();
        exportUtils = new ExportUtils(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chuck_fragment_transaction_list, container, false);
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                    DividerItemDecoration.VERTICAL));
            adapter = new TransactionAdapter(getContext(), listener);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            listener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chuck_main, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clear) {
            getContext().getContentResolver().delete(ChuckContentProvider.TRANSACTION_URI, null, null);
            NotificationHelper.clearBuffer();
            exportUtils.delete(appExecutors);
            return true;
        } else if (item.getItemId() == R.id.browse_sql) {
            SQLiteUtils.browseDatabase(getContext());
            return true;
        } else if (item.getItemId() == R.id.settings) {
            if (getContext() != null) {
                SettingsActivity.Companion.start(this, ACTIVITY_REQUEST_CODE_SETTINGS);
            }
            return true;
        } else if (item.getItemId() == R.id.export) {
            exportUtils.export(appExecutors, mProjection, mSelection, mSelectionArgs, mSortOrder);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getContext());
        loader.setUri(ChuckContentProvider.TRANSACTION_URI);

        StringBuilder finalSelection = new StringBuilder();
        ArrayList<String> settingSelections = new ArrayList<>();

        String searchSelection = "";
        ArrayList<String> selectionArgs = new ArrayList<>();

        if (settingsManager != null) {
            if (settingsManager.isError400FilterEnabled()) {
                settingSelections.add("responseCode LIKE ? ");
                selectionArgs.add("4%");
            }

            if (settingsManager.isError500FilterEnabled()) {
                settingSelections.add("responseCode LIKE ? ");
                selectionArgs.add("5%");
            }

            if (settingsManager.isErrorMalformedJsonFilterEnabled()) {
                settingSelections.add("malformedJson LIKE ? ");
                selectionArgs.add("1");
            }
        }

        if (!TextUtils.isEmpty(currentFilter)) {
            if (TextUtils.isDigitsOnly(currentFilter)) {
                searchSelection = "(responseCode LIKE ? OR path LIKE ?)";

                selectionArgs.add(currentFilter + "%");
                selectionArgs.add("%" + currentFilter + "%");
            } else {
                searchSelection = "(path LIKE ?)";
                selectionArgs.add("%" + currentFilter + "%");
            }
        }

        // Build Settings selection String
        finalSelection.append(buildSettingsSelection(settingSelections));
        finalSelection.append(searchSelection);

        mSelection = finalSelection.toString();
        mSelectionArgs = selectionArgs.toArray(new String[0]);
        mProjection = HttpTransaction.PARTIAL_PROJECTION;
        mSortOrder = "requestDate DESC";

        loader.setSelection(mSelection);
        loader.setSelectionArgs(mSelectionArgs);

        loader.setProjection(mProjection);
        loader.setSortOrder(mSortOrder);

        return loader;
    }

    private String buildSettingsSelection(ArrayList<String> settingSelections) {
        StringBuilder result = new StringBuilder();

        if (settingSelections.size() > 0) {
            result.append("(");
        }

        for (int i = 0; i < settingSelections.size(); i++) {
            result.append(settingSelections.get(i));

            if (i < settingSelections.size() - 1) {
                result.append(" OR ");
            }
        }

        if (settingSelections.size() > 0) {
            result.append(")");

            if (!TextUtils.isEmpty(currentFilter)) {
                result.append(" AND ");
            }
        }

        return result.toString();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        currentFilter = newText;
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_REQUEST_CODE_SETTINGS) {
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(HttpTransaction item);
    }
}
