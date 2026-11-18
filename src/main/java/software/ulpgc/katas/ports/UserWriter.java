package software.ulpgc.katas.ports;

import software.ulpgc.katas.domain.model.CorporateDirectoryReport;

public interface UserWriter {
    void writeReport(CorporateDirectoryReport report);
}
