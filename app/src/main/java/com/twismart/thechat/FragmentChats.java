package com.twismart.thechat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mysampleapp.demo.nosql.UserDO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FragmentChats extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private RecyclerView recyclerChats;
    private ChatsAdapter adapter;

    public FragmentChats() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerChats = (RecyclerView) v.findViewById(R.id.recyclerChats);
        recyclerChats.setHasFixedSize(true);
        recyclerChats.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        PreferencesProfile preferencesProfile = new PreferencesProfile(getContext());
        final NetworkInteractor networkInteractor = new NetworkInteractor(getActivity());

        networkInteractor.getUserById(preferencesProfile.getId(), new NetworkInteractor.IGetUserById() {
            @Override
            public void onSucces(final UserDO userDO) {
                List<String> myChatsCurrent = new ArrayList<>(Arrays.asList(userDO.getChatsCurrent().substring(1, userDO.getChatsCurrent().length()-1).split("\\s*,\\s*")));
                networkInteractor.getUsersByIds(myChatsCurrent, new NetworkInteractor.IGetUsersByIds() {
                    @Override
                    public void onSucces(List<UserDO> listUsers) {
                        adapter = new ChatsAdapter(getContext(), listUsers, userDO, FragmentChats.this);
                        recyclerChats.setAdapter(adapter);
                    }

                    @Override
                    public void onFailure(String error) {

                    }
                });
            }

            @Override
            public void onFailure(String error) {

            }
        });
        return v;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constantes.ID, adapter.users.get(recyclerChats.getChildAdapterPosition(view)).getUserId());
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
