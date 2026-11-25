package com.hcl.hrgenius.service;

import com.hcl.hrgenius.dto.payroll.PayrollRequest;
import com.hcl.hrgenius.dto.payroll.PayrollResponse;
import com.hcl.hrgenius.entity.Payroll;
import com.hcl.hrgenius.exception.ResourceNotFoundException;
import com.hcl.hrgenius.repository.PayrollRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PayrollService {

    private final PayrollRepository payrollRepository;

    public PayrollService(PayrollRepository payrollRepository) {
        this.payrollRepository = payrollRepository;
    }

    @Transactional(readOnly = true)
    public List<PayrollResponse> getPayrolls(String search, String status) {
        List<Payroll> list;
        if ((search == null || search.trim().isEmpty()) && (status == null || status.trim().isEmpty() || "All".equalsIgnoreCase(status))) {
            list = payrollRepository.findAll();
        } else {
            list = payrollRepository.searchPayroll(search, status);
        }
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PayrollResponse getPayrollById(Long id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with id: " + id));
        return mapToResponse(payroll);
    }

    public PayrollResponse createPayroll(PayrollRequest request) {
        Payroll payroll = new Payroll();
        payroll.setEmployee(request.getEmployee());
        payroll.setEmployeeId(request.getEmployeeId());
        payroll.setSalary(request.getSalary() != null ? request.getSalary() : BigDecimal.ZERO);
        payroll.setAllowances(request.getAllowances() != null ? request.getAllowances() : BigDecimal.ZERO);
        payroll.setDeductions(request.getDeductions() != null ? request.getDeductions() : BigDecimal.ZERO);
        payroll.setBonus(request.getBonus() != null ? request.getBonus() : BigDecimal.ZERO);

        if (request.getNetSalary() != null) {
            payroll.setNetSalary(request.getNetSalary());
        } else {
            payroll.calculateNetSalary();
        }

        payroll.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        payroll.setPayMonth(request.getPayMonth() != null ? request.getPayMonth() : "Current Month");
        payroll.setStatus(request.getStatus() != null ? request.getStatus() : "Paid");

        Payroll saved = payrollRepository.save(payroll);
        return mapToResponse(saved);
    }

    public PayrollResponse updatePayroll(Long id, PayrollRequest request) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with id: " + id));

        if (request.getEmployee() != null) payroll.setEmployee(request.getEmployee());
        if (request.getEmployeeId() != null) payroll.setEmployeeId(request.getEmployeeId());
        if (request.getSalary() != null) payroll.setSalary(request.getSalary());
        if (request.getAllowances() != null) payroll.setAllowances(request.getAllowances());
        if (request.getDeductions() != null) payroll.setDeductions(request.getDeductions());
        if (request.getBonus() != null) payroll.setBonus(request.getBonus());

        if (request.getNetSalary() != null) {
            payroll.setNetSalary(request.getNetSalary());
        } else {
            payroll.calculateNetSalary();
        }

        if (request.getPaymentDate() != null) payroll.setPaymentDate(request.getPaymentDate());
        if (request.getPayMonth() != null) payroll.setPayMonth(request.getPayMonth());
        if (request.getStatus() != null) payroll.setStatus(request.getStatus());

        Payroll updated = payrollRepository.save(payroll);
        return mapToResponse(updated);
    }

    public void deletePayroll(Long id) {
        if (!payrollRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payroll record not found with id: " + id);
        }
        payrollRepository.deleteById(id);
    }

    private PayrollResponse mapToResponse(Payroll payroll) {
        PayrollResponse resp = new PayrollResponse();
        resp.setId(payroll.getId());
        resp.setEmployee(payroll.getEmployee());
        resp.setEmployeeId(payroll.getEmployeeId());
        resp.setSalary(payroll.getSalary());
        resp.setAllowances(payroll.getAllowances());
        resp.setDeductions(payroll.getDeductions());
        resp.setBonus(payroll.getBonus());
        resp.setNetSalary(payroll.getNetSalary());
        resp.setPaymentDate(payroll.getPaymentDate());
        resp.setPayMonth(payroll.getPayMonth());
        resp.setStatus(payroll.getStatus());
        return resp;
    }
}
