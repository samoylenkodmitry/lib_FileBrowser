package com.samart.filebrowser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

public class DirChooserDialog {

    private final View view;
    private final ListBaseAdapter adapter;
    private final FileBrowser fileBrowser;
    private final ParentListAdapter parentListAdapter;
    private final TextView textCurrentDir;
    private final Mediator mediator;

    public DirChooserDialog(final LayoutInflater inflater, final File startDir) {
        view = inflater.inflate(R.layout.file_list, null);
        fileBrowser = new FileBrowser(startDir, true);
        final ListView parentList = (ListView) view.findViewById(R.id.parent_list);
        mediator = new Mediator();
        parentListAdapter = new ParentListAdapter(mediator, inflater, fileBrowser);
        parentList.setAdapter(parentListAdapter);
        adapter = new ListBaseAdapter(mediator, inflater, fileBrowser);
        final ListView list = (ListView) view.findViewById(R.id.list);
        list.setAdapter(adapter);
        textCurrentDir = (TextView) view.findViewById(R.id.text_dir);
        mediator.notifyFileBrowse();
    }

    public View getView() {
        return this.view;
    }

    /**
     * return selected directory or null if no item selected
     *
     * @return selected dir or null
     */
    public String getDirectory() {
        final int checkedItem = adapter.getCheckedItem();
        if (-1 == checkedItem) {
            return null;
        }
        return fileBrowser.getAbsolutePath(checkedItem);
    }

    /**
     * use this only from root directory
     *
     * @param dir directory to navigate from root
     */
    public void navigateTo(File dir) {
        fileBrowser.navigateTo(dir);
        mediator.notifyFileBrowse();
    }

    private static class ParentListAdapter extends BaseAdapter {
        private final FileBrowser fileBrowser;
        private final LayoutInflater inflater;
        private final Mediator mediator;

        public ParentListAdapter(final Mediator mediator, final LayoutInflater inflater, final FileBrowser fileBrowser) {
            this.fileBrowser = fileBrowser;
            this.inflater = inflater;
            this.mediator = mediator;
        }

        @Override
        public int getCount() {
            return fileBrowser.getParentDirs().size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.parent_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.text = (TextView) convertView.findViewById(R.id.textView);
                convertView.setTag(viewHolder);
            }
            final String name = fileBrowser.getParentDirs().get(position).getName();
            viewHolder = (ViewHolder) convertView.getTag();
            if (!name.equals(viewHolder.filename)) {
                viewHolder.filename = name;
                viewHolder.text.setText(name + "/");
                viewHolder.text.setOnClickListener(new ParentListClick(mediator, position, fileBrowser));
            }
            return convertView;
        }

        private class ViewHolder {
            TextView text;
            String filename;
        }

    }

    private static class ParentListClick implements View.OnClickListener {
        private final int position;
        private final FileBrowser fileBrowser;
        private final Mediator mediator;

        public ParentListClick(final Mediator mediator, int position, final FileBrowser fileBrowser) {
            this.position = position;
            this.fileBrowser = fileBrowser;
            this.mediator = mediator;
        }

        @Override
        public void onClick(View v) {
            fileBrowser.goToParent(position);
            mediator.notifyFileBrowse();
        }
    }

    private static class ListBaseAdapter extends BaseAdapter {
        private final LayoutInflater inflater;
        private final FileBrowser fileBrowser;
        private final Mediator mediator;
        private int checkedItem = -1;

        public ListBaseAdapter(final Mediator mediator, final LayoutInflater inflater, final FileBrowser fileBrowser) {
            this.inflater = inflater;
            this.fileBrowser = fileBrowser;
            this.mediator = mediator;
        }

        @Override
        public int getCount() {
            return fileBrowser.getFiles().size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.file_item, null);
                viewHolder = new ViewHolder();
                viewHolder.textView = (TextView) convertView.findViewById(R.id.file_text);
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.file_check);
                convertView.setTag(viewHolder);
            }
            viewHolder = (ViewHolder) convertView.getTag();
            if (position == checkedItem) {
                viewHolder.checkBox.setChecked(true);
            } else {
                viewHolder.checkBox.setChecked(false);
            }
            final String name = fileBrowser.getName(position);
            if (!name.equals(viewHolder.name)) {
                viewHolder.textView.setText(name);
                viewHolder.checkBox.setOnClickListener(new OnCheckClick(position));
                viewHolder.textView.setOnClickListener(new OnFileClickListener(mediator, position, fileBrowser));
            }
            return convertView;
        }

        public int getCheckedItem() {
            return checkedItem;
        }

        public void uncheck() {
            checkedItem = -1;
        }

        private static class ViewHolder {
            TextView textView;
            CheckBox checkBox;
            String name;
        }

        private class OnCheckClick implements View.OnClickListener {
            private final int position;

            public OnCheckClick(int position) {
                this.position = position;
            }

            @Override
            public void onClick(View v) {
                File file = fileBrowser.getFile(position);
                if (file.canWrite()) {
                    checkedItem = position;
                }
                notifyDataSetChanged();
            }
        }

    }

    private static class OnFileClickListener implements View.OnClickListener {

        private final int position;
        private final FileBrowser fileBrowser;
        private final Mediator mediator;

        public OnFileClickListener
                (final Mediator mediator, int position, final FileBrowser fileBrowser) {
            this.position = position;
            this.fileBrowser = fileBrowser;
            this.mediator = mediator;
        }

        @Override
        public void onClick(View v) {
            if (fileBrowser.goDir(position))
                mediator.notifyFileBrowse();
        }
    }

    private class Mediator {
        public void notifyFileBrowse() {
            adapter.uncheck();
            adapter.notifyDataSetChanged();
            parentListAdapter.notifyDataSetChanged();
            textCurrentDir.setText(fileBrowser.getCurrentDir().getName() + "/");
        }
    }
}
