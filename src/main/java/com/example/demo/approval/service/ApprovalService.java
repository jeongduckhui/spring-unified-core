package com.example.demo.approval.service;

import com.example.demo.approval.domain.Approval;
import com.example.demo.approval.repository.ApprovalRepository;
import com.example.demo.soap.approval.client.ApprovalSoapClient;
import com.example.demo.soap.approval.mapper.ApprovalSoapMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final ApprovalSoapClient soapClient;
    private final ApprovalSoapMapper mapper;

    @Transactional
    public void requestApproval(Long approvalId) {

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow();

        var request = mapper.toRequest(approval);

        try {

            var response = soapClient.send(approvalId, request);

            if ("SUCCESS".equals(response.getResult())) {
                approval.markProcessing(response.getExternalId());
            } else {
                approval.markFailed();
            }

        } catch (Exception e) {

            approval.markFailed();
        }
    }
}