package com.twismart.thechat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mysampleapp.demo.nosql.UserDO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class FragmentChats extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private RecyclerView recyclerChats;
    private SwipeRefreshLayout refreshLayout;
    private ChatsAdapter adapter = null;

    private PreferencesProfile preferencesProfile;
    private NetworkInteractor networkInteractor;

    public FragmentChats() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chats, container, false);

        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateChats();
            }
        });
        refreshLayout.setRefreshing(true);

        recyclerChats = (RecyclerView) v.findViewById(R.id.recyclerChats);
        recyclerChats.setHasFixedSize(true);
        recyclerChats.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        preferencesProfile = new PreferencesProfile(getContext());
        networkInteractor = new NetworkInteractor(getActivity());

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateChats();
    }

    private void updateChats(){
        networkInteractor.getUserById(preferencesProfile.getId(), new NetworkInteractor.IGetUserById() {
            @Override
            public void onSucces(final UserDO userDO) {
                try {
                    List<String> myChatsCurrent = new ArrayList<>(Arrays.asList(userDO.getChatsCurrent().substring(1, userDO.getChatsCurrent().length() - 1).split("\\s*,\\s*")));
                    networkInteractor.getUsersByIds(myChatsCurrent, new NetworkInteractor.IGetUsersByIds() {
                        @Override
                        public void onSucces(List<UserDO> listUsers) {
                            if (adapter == null) {
                                adapter = new ChatsAdapter(getContext(), listUsers, userDO, FragmentChats.this);
                                recyclerChats.setAdapter(adapter);
                            } else {
                                adapter.setListUsers(listUsers);
                                adapter.notifyDataSetChanged();
                            }
                            refreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void onFailure(String error) {
                            try {
                                Toast.makeText(getContext(), R.string.text_error_internet, Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e){
                                Log.d("text_error_internet", "text_error_internet");
                            }
                            refreshLayout.setRefreshing(false);
                        }
                    });
                }
                catch (Exception e){
                    Log.e("catch updateChats", "error: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(String error) {
                try {
                    refreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), R.string.text_error_internet, Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    Log.d("text_error_internet", "text_error_internet");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constantes.ID, adapter.users.get(recyclerChats.getChildAdapterPosition(view)).getUserId());
        intent.putExtra(Constantes.TOKEN_ID, adapter.users.get(recyclerChats.getChildAdapterPosition(view)).getTokenId());
        intent.putExtra(Constantes.NAME, adapter.users.get(recyclerChats.getChildAdapterPosition(view)).getName());
        startActivity(intent);
    }




    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
           //throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
