package com.example.demo.soap.approval.mapper;

import com.example.demo.approval.domain.Approval;
import com.example.demo.soap.approval.dto.SoapApproveRequest;
import org.springframework.stereotype.Component;

@Component
public class ApprovalSoapMapper {

    public SoapApproveRequest toRequest(Approval approval) {
        SoapApproveRequest req = new SoapApproveRequest();
        req.setDocId(String.valueOf(approval.getId()));
        req.setRequesterId(String.valueOf(approval.getRequestUserId()));
        req.setTitle(approval.getTitle());
        req.setContent(approval.getContent());
        return req;
    }
}