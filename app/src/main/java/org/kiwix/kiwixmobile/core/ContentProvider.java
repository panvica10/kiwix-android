package org.kiwix.kiwixmobile.core;

import org.kiwix.kiwixmobile.library.entity.LibraryNetworkEntity;

public class ContentProvider {

    private static ContentProvider instance;

    private LibraryNetworkEntity libraryNetworkEntity;

    public static ContentProvider getContentProvider(){
        if (instance == null) {
            instance = new ContentProvider();
        }
        return  instance;
    }

    public LibraryNetworkEntity getLibraryNetworkEntity() {
        return libraryNetworkEntity;
    }

    public void setLibraryNetworkEntity(LibraryNetworkEntity libraryNetworkEntity) {
        this.libraryNetworkEntity = libraryNetworkEntity;
    }
}
