package com.yuzj.autolink.dao;

import com.yuzj.autolink.domain.PlcDataRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Service;

/**
 * @author yuzj002
 */
@Service
public class PlcDataRecordResoprist {


    public ObservableList<PlcDataRecord> list() {

        return FXCollections.observableArrayList();
    }
}
