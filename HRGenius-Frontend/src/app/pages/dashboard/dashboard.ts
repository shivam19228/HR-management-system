import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgIf, NgFor, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import {
  HrDataService,
  ActivityItem,
  DepartmentItem,
  EmployeeItem,
  CandidateItem,
  InterviewItem,
  LeaveItem,
  PayrollItem,
  PerformanceItem,
  TrainingItem,
  UserItem
} from '../../services/hr-data';
import { AuthService, User } from '../../services/auth';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, NgIf, NgFor, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit, OnDestroy {
  currentUser: User | null = null;
  userRole: 'ADMIN' | 'HR_MANAGER' | 'EMPLOYEE' | 'GUEST' = 'ADMIN';
  private authSub: Subscription | null = null;

  // Global / Admin & Manager stats
  stats = {
    totalEmployees: 0,
    totalDepartments: 0,
    totalCandidates: 0,
    presentToday: 0,
    pendingLeaves: 0,
    totalPayroll: 0,
    avgSalary: 0,
    openJobs: 0
  };

  recentActivities: ActivityItem[] = [];
  departments: DepartmentItem[] = [];
  recentEmployees: EmployeeItem[] = [];
  recentCandidates: CandidateItem[] = [];
  upcomingInterviews: InterviewItem[] = [];
  pendingLeavesList: LeaveItem[] = [];
  systemUsers: UserItem[] = [];

  // Employee personal workplace data
  myProfile: EmployeeItem | null = null;
  myAttendance: AttendanceItemDisplay[] = [];
  myLeaves: LeaveItem[] = [];
  myPayroll: PayrollItem | null = null;
  myPerformance: PerformanceItem | null = null;
  myTrainings: TrainingItem[] = [];
  isClockedIn: boolean = false;
  todayClockInTime: string = '';
  actionSuccessMessage: string = '';
  showLeaveModal: boolean = false;

  leaveForm = {
    leaveType: 'Casual Leave' as 'Casual Leave' | 'Sick Leave' | 'Earned Leave' | 'Emergency Leave' | 'Maternity Leave',
    startDate: new Date().toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0],
    reason: ''
  };

  constructor(
    private hrData: HrDataService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.authSub = this.authService.currentUser$.subscribe(u => {
      this.currentUser = u;
      this.userRole = this.authService.getRole();
      this.loadDashboard();
    });
  }

  ngOnDestroy(): void {
    if (this.authSub) {
      this.authSub.unsubscribe();
    }
  }

  get isRoleAdmin(): boolean {
    return this.userRole === 'ADMIN';
  }

  get isRoleManager(): boolean {
    return this.userRole === 'HR_MANAGER';
  }

  get isRoleEmployee(): boolean {
    return this.userRole === 'EMPLOYEE';
  }

  loadDashboard(): void {
    if (this.isRoleEmployee) {
      this.loadEmployeeWorkspace();
    } else if (this.isRoleManager) {
      this.loadManagerDashboard();
    } else {
      this.loadAdminDashboard();
    }
  }

  // Admin Dashboard Loading
  loadAdminDashboard(): void {
    this.stats = this.hrData.getDashboardStats();
    this.recentActivities = this.hrData.getActivities().slice(0, 6);
    this.departments = this.hrData.getDepartments().slice(0, 5);
    this.recentEmployees = this.hrData.getEmployees().slice(0, 5);
    this.systemUsers = this.hrData.getUsers().slice(0, 5);

    this.hrData.fetchDashboardStats().subscribe({
      next: (s) => {
        if (s) {
          this.stats = {
            totalEmployees: Number(s.totalEmployees) || 0,
            totalDepartments: Number(s.totalDepartments) || 0,
            totalCandidates: Number(s.totalCandidates) || 0,
            presentToday: Number(s.presentToday) || 0,
            pendingLeaves: Number(s.pendingLeaves) || 0,
            totalPayroll: Number(s.totalPayroll) || 0,
            avgSalary: Number(s.avgSalary) || 0,
            openJobs: Number(s.openJobs) || 0
          };
        }
      }
    });

    this.hrData.fetchDepartments().subscribe({
      next: (deps) => { if (deps?.length) this.departments = deps.slice(0, 5); }
    });

    this.hrData.fetchEmployees().subscribe({
      next: (emps) => { if (emps?.length) this.recentEmployees = emps.slice(0, 5); }
    });

    this.hrData.fetchUsers().subscribe({
      next: (users) => { if (users?.length) this.systemUsers = users.slice(0, 5); }
    });
  }

  // HR Manager Dashboard Loading
  loadManagerDashboard(): void {
    this.stats = this.hrData.getDashboardStats();
    this.recentActivities = this.hrData.getActivities().slice(0, 6);
    this.departments = this.hrData.getDepartments().slice(0, 5);
    this.recentCandidates = this.hrData.getCandidates().slice(0, 5);
    this.upcomingInterviews = this.hrData.getInterviews().slice(0, 4);
    this.refreshManagerPendingLeaves();

    this.hrData.fetchDashboardStats().subscribe({
      next: (s) => {
        if (s) {
          this.stats = {
            totalEmployees: Number(s.totalEmployees) || 0,
            totalDepartments: Number(s.totalDepartments) || 0,
            totalCandidates: Number(s.totalCandidates) || 0,
            presentToday: Number(s.presentToday) || 0,
            pendingLeaves: Number(s.pendingLeaves) || 0,
            totalPayroll: 0, // Hidden for HR Manager
            avgSalary: 0,    // Hidden for HR Manager
            openJobs: Number(s.openJobs) || 0
          };
        }
      }
    });

    this.hrData.fetchCandidates().subscribe({
      next: (cands) => { if (cands?.length) this.recentCandidates = cands.slice(0, 5); }
    });

    this.hrData.fetchInterviews().subscribe({
      next: (ints) => { if (ints?.length) this.upcomingInterviews = ints.slice(0, 4); }
    });

    this.hrData.fetchEmployees().subscribe();

    this.hrData.fetchDepartments().subscribe({
      next: (deps) => { if (deps?.length) this.departments = deps.slice(0, 5); }
    });

    this.hrData.fetchLeaves().subscribe({
      next: () => this.refreshManagerPendingLeaves()
    });
  }

  refreshManagerPendingLeaves(): void {
    const allLeaves = this.hrData.getLeaves();
    this.pendingLeavesList = allLeaves.filter(l => l.status === 'Pending').slice(0, 5);
    this.stats.pendingLeaves = allLeaves.filter(l => l.status === 'Pending').length;
  }

  approveLeave(leaveId: number): void {
    this.hrData.updateLeave(leaveId, { status: 'Approved' });
    this.actionSuccessMessage = `Leave request #${leaveId} approved successfully.`;
    this.refreshManagerPendingLeaves();
    setTimeout(() => this.actionSuccessMessage = '', 4000);
  }

  rejectLeave(leaveId: number): void {
    this.hrData.updateLeave(leaveId, { status: 'Rejected' });
    this.actionSuccessMessage = `Leave request #${leaveId} rejected.`;
    this.refreshManagerPendingLeaves();
    setTimeout(() => this.actionSuccessMessage = '', 4000);
  }

  // Employee Personal Workspace Loading
  loadEmployeeWorkspace(): void {
    const user = this.currentUser;
    const userName = (user?.name || '').trim().toLowerCase();
    const userEmpId = String(user?.empId || user?.employeeId || '').trim().toLowerCase();

    // 1. Resolve Profile
    const employees = this.hrData.getEmployees();
    const matchedEmp = employees.find(e =>
      (e.name && e.name.toLowerCase() === userName) ||
      (e.empId && e.empId.toLowerCase() === userEmpId) ||
      (user?.email && e.email && e.email.toLowerCase() === user.email.toLowerCase())
    );

    if (matchedEmp) {
      this.myProfile = matchedEmp;
    } else {
      this.myProfile = {
        id: user?.id || 3,
        empId: user?.empId || 'EMP-003',
        name: user?.name || 'Jessica Taylor',
        email: user?.email || 'jessica@hrgenius.com',
        phone: '+1 (555) 345-6789',
        department: 'Software Engineering',
        job: 'Software Developer',
        joiningDate: '2023-01-15',
        status: 'Active',
        salary: 82000
      };
    }

    this.hrData.fetchEmployees().subscribe({
      next: (emps) => {
        if (emps && emps.length) {
          const fresh = emps.find(e =>
            (e.name && e.name.toLowerCase() === userName) ||
            (e.empId && e.empId.toLowerCase() === userEmpId) ||
            (user?.email && e.email && e.email.toLowerCase() === user.email.toLowerCase())
          );
          if (fresh) {
            this.myProfile = fresh;
          }
        }
      }
    });

    // 2. Fetch Personal Leaves
    this.hrData.fetchLeaves().subscribe({
      next: (leaves) => {
        const pool = leaves?.length ? leaves : this.hrData.getLeaves();
        this.myLeaves = pool.filter(l =>
          (l.employee && l.employee.toLowerCase() === userName) ||
          (l.employeeId && l.employeeId.toLowerCase() === userEmpId)
        );
      }
    });

    // 3. Fetch Personal Attendance
    this.hrData.fetchAttendance().subscribe({
      next: (atts) => {
        const pool = atts?.length ? atts : this.hrData.getAttendance();
        const myAtts = pool.filter(a =>
          (a.employee && a.employee.toLowerCase() === userName) ||
          (a.employeeId && a.employeeId.toLowerCase() === userEmpId)
        );
        this.myAttendance = myAtts.slice(0, 5);

        // Check today's clock in
        const todayStr = new Date().toISOString().split('T')[0];
        const todayRec = myAtts.find(a => a.date === todayStr);
        if (todayRec) {
          this.isClockedIn = true;
          this.todayClockInTime = todayRec.checkIn || '09:00 AM';
        }
      }
    });

    // 4. Fetch Personal Payroll (Strictly personal record)
    this.hrData.fetchPayroll().subscribe({
      next: (payrolls) => {
        const pool = payrolls?.length ? payrolls : this.hrData.getPayroll();
        const myPay = pool.find(p =>
          (p.employee && p.employee.toLowerCase() === userName) ||
          (p.employeeId && p.employeeId.toLowerCase() === userEmpId)
        );
        if (myPay) {
          this.myPayroll = myPay;
        } else if (pool.length > 0 && this.isRoleEmployee) {
          // If backend already filtered to 1 record
          this.myPayroll = pool[0];
        }
      }
    });

    // 5. Fetch Personal Performance Review
    this.hrData.fetchPerformance().subscribe({
      next: (reviews) => {
        const pool = reviews?.length ? reviews : this.hrData.getPerformance();
        const myPerf = pool.find(p =>
          (p.employee && p.employee.toLowerCase() === userName) ||
          (p.employeeId && p.employeeId.toLowerCase() === userEmpId)
        );
        if (myPerf) {
          this.myPerformance = myPerf;
        } else if (pool.length > 0) {
          this.myPerformance = pool[0];
        }
      }
    });

    // 6. Fetch Assigned Trainings
    this.hrData.fetchTrainings().subscribe({
      next: (trainings) => {
        const pool = trainings?.length ? trainings : this.hrData.getTrainings();
        this.myTrainings = pool.filter(t =>
          !t.employee || t.employee.toLowerCase() === userName || t.employee.toLowerCase() === 'all'
        ).slice(0, 4);
      }
    });
  }

  // Interactive Attendance Clock In / Out
  toggleClockInOut(): void {
    const today = new Date();
    const timeStr = today.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const dateStr = today.toISOString().split('T')[0];

    if (!this.isClockedIn) {
      this.isClockedIn = true;
      this.todayClockInTime = timeStr;
      this.hrData.addAttendance({
        employee: this.currentUser?.name || 'Jessica Taylor',
        employeeId: this.currentUser?.empId || 'EMP-003',
        date: dateStr,
        checkIn: timeStr,
        checkOut: '-',
        status: 'Present',
        remarks: 'Self clocked-in via employee portal'
      });
      this.actionSuccessMessage = `Clocked in successfully at ${timeStr}. Have a great workday!`;
    } else {
      this.isClockedIn = false;
      this.actionSuccessMessage = `Clocked out successfully at ${timeStr}. See you tomorrow!`;
    }
    setTimeout(() => this.actionSuccessMessage = '', 4000);
  }

  // Modal Actions
  openLeaveModal(): void {
    this.showLeaveModal = true;
    this.leaveForm = {
      leaveType: 'Casual Leave',
      startDate: new Date().toISOString().split('T')[0],
      endDate: new Date().toISOString().split('T')[0],
      reason: ''
    };
  }

  closeLeaveModal(): void {
    this.showLeaveModal = false;
  }

  submitLeaveRequest(): void {
    if (!this.leaveForm.reason.trim()) {
      alert('Please provide a reason for the leave request.');
      return;
    }

    const start = new Date(this.leaveForm.startDate);
    const end = new Date(this.leaveForm.endDate);
    const diffTime = Math.abs(end.getTime() - start.getTime());
    const totalDays = Math.max(1, Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1);

    const newLeave = this.hrData.addLeave({
      employee: this.currentUser?.name || 'Jessica Taylor',
      employeeId: this.currentUser?.empId || 'EMP-003',
      leaveType: this.leaveForm.leaveType,
      startDate: this.leaveForm.startDate,
      endDate: this.leaveForm.endDate,
      totalDays,
      reason: this.leaveForm.reason,
      status: 'Pending'
    });

    this.myLeaves.unshift(newLeave);
    this.showLeaveModal = false;
    this.actionSuccessMessage = 'Leave request submitted successfully. Your manager has been notified.';
    setTimeout(() => this.actionSuccessMessage = '', 5000);
  }

  getAttendancePercentage(): number {
    if (this.stats.totalEmployees === 0) return 0;
    return Math.round((this.stats.presentToday / this.stats.totalEmployees) * 100);
  }

  formatCurrency(value: number | undefined): string {
    if (value === undefined || value === null) return '$0';
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(value);
  }
}

interface AttendanceItemDisplay {
  id: number;
  employee: string;
  employeeId?: string;
  date: string;
  checkIn: string;
  checkOut: string;
  status: 'Present' | 'Absent' | 'Late' | 'Half Day';
  remarks?: string;
}
