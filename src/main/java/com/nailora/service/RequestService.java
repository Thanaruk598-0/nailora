package com.nailora.service;

import com.nailora.dto.BookingRequest_1;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RequestService {
    private final List<BookingRequest_1> requests = new ArrayList<>();
    private Long nextId = 1L;

    public List<BookingRequest_1> getAllRequests() {
        return requests;
    }

    public BookingRequest_1 createRequest(BookingRequest_1 req) {
        // mock เก็บใน memory (ยังไม่ต่อ DB)
        try {
            var f = req.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(req, nextId++);
        } catch (Exception ignored) {}
        requests.add(req);
        return req;
    }

    public BookingRequest_1 updateStatus(Long id, String status) {
        for (BookingRequest_1 req : requests) {
            try {
                var f = req.getClass().getDeclaredField("id");
                f.setAccessible(true);
                Long reqId = (Long) f.get(req);
                if (reqId.equals(id)) {
                    var s = req.getClass().getDeclaredField("status");
                    s.setAccessible(true);
                    s.set(req, status);
                    return req;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }
}
