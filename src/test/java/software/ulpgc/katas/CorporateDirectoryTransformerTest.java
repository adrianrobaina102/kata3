package software.ulpgc.katas;

import org.junit.jupiter.api.Test;
import software.ulpgc.katas.domain.model.CorporateDirectoryReport;
import software.ulpgc.katas.domain.model.User;
import software.ulpgc.katas.domain.service.CorporateDirectoryTransformer;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CorporateDirectoryTransformerTest {

    @Test
    void shouldFilterBlankEmailsSortByIdAndComputeMetadata() {
        CorporateDirectoryTransformer transformer = new CorporateDirectoryTransformer();
        List<User> input = List.of(
                new User(2, "Ervin Howell", "Antonette", "Shanna@melissa.tv"),
                new User(3, "No Email", "noemail", "   "),
                new User(1, "Leanne Graham", "Bret", "Sincere@april.biz")
        );

        CorporateDirectoryReport report = transformer.transform(input, LocalDate.of(2026, 10, 22));

        assertEquals(2, report.metadata().totalUsers());
        assertEquals("2026-10-22", report.metadata().reportDate());
        assertEquals("Corporate Directory Report", report.metadata().purpose());
        assertEquals(List.of(1, 2), report.users().stream().map(User::id).toList());
    }
}
