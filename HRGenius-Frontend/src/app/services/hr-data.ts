import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';

export interface EmployeeItem {
  id: number;
  empId: string;
  name: string;
  firstName?: string;
  lastName?: string;
  email: string;
  phone: string;
  department: string;
  departmentId?: number;
  job: string;
  designation?: string;
  joiningDate: string;
  status: 'Active' | 'On Leave' | 'Terminated';
  salary?: number | string;
  gender?: string;
  employmentType?: string;
  city?: string;
  address?: string;
}

export interface DepartmentItem {
  id: number;
  departmentId: string;
  name: string;
  code: string;
  description: string;
  manager: string;
  employeeCount?: number;
}

export interface JobItem {
  id: number;
  jobId: string;
  jobTitle: string;
  department: string;
  description: string;
  status: 'Open' | 'Closed' | 'Draft';
  location?: string;
  employmentType?: string;
  experienceRequired?: string;
  salary?: string;
}

export interface CandidateItem {
  id: number;
  candidateId: string;
  name: string;
  firstName?: string;
  lastName?: string;
  email: string;
  phone: string;
  appliedJob: string;
  status: 'Applied' | 'Screening' | 'Interviewing' | 'Offered' | 'Rejected' | 'Hired';
  qualification?: string;
  experience?: string;
  resumeUrl?: string;
  applicationDate?: string;
}

export interface AttendanceItem {
  id: number;
  employee: string;
  employeeId?: string;
  date: string;
  checkIn: string;
  checkOut: string;
  status: 'Present' | 'Absent' | 'Late' | 'Half Day';
  remarks?: string;
}

export interface LeaveItem {
  id: number;
  employee: string;
  employeeId?: string;
  leaveType: 'Casual Leave' | 'Sick Leave' | 'Earned Leave' | 'Emergency Leave' | 'Maternity Leave';
  startDate: string;
  endDate: string;
  totalDays: number;
  appliedDate?: string;
  reason: string;
  status: 'Pending' | 'Approved' | 'Rejected';
}

export interface PayrollItem {
  id: number;
  employee: string;
  employeeId?: string;
  salary: number;
  allowances: number;
  deductions: number;
  bonus: number;
  netSalary: number;
  paymentDate: string;
  payMonth: string;
  status: 'Paid' | 'Pending';
}

export interface PerformanceItem {
  id: number;
  employee: string;
  employeeId?: string;
  reviewDate: string;
  rating: number; // 1 to 5
  goals: string;
  achievements: string;
  feedback: string;
}

export interface InterviewItem {
  id: number;
  candidate: string;
  job: string;
  interviewDate: string;
  interviewer: string;
  mode: 'Online' | 'In-Person';
  status: 'Scheduled' | 'Completed' | 'Cancelled';
  feedback: string;
}

export interface TrainingItem {
  id: number;
  employee: string;
  trainingName: string;
  trainer: string;
  startDate: string;
  endDate: string;
  status: 'Upcoming' | 'In Progress' | 'Completed';
}

export interface DocumentItem {
  id: number;
  employee: string;
  documentName: string;
  documentType: 'ID Proof' | 'Resume' | 'Contract' | 'Certificate' | 'Policy';
  documentUrl: string;
  uploadDate: string;
  status: 'Verified' | 'Pending' | 'Expired';
}

export interface UserItem {
  id: number;
  username: string;
  employee: string;
  role: 'Admin' | 'HR Manager' | 'Recruiter' | 'Employee';
  status: 'Active' | 'Inactive';
  email: string;
}

export interface ActivityItem {
  id: number;
  action: string;
  target: string;
  time: string;
  user: string;
  type: 'employee' | 'leave' | 'candidate' | 'payroll' | 'training';
}

export interface DashboardStats {
  totalEmployees: number;
  totalDepartments: number;
  totalCandidates: number;
  presentToday: number;
  pendingLeaves: number;
  totalPayroll: number;
  avgSalary: number;
  openJobs: number;
}

@Injectable({
  providedIn: 'root'
})
export class HrDataService {
  private readonly STORAGE_KEY = 'hrgenius_master_data_v1';
  private readonly apiUrl = environment.apiUrl;

  private employees: EmployeeItem[] = [];
  private departments: DepartmentItem[] = [];
  private jobs: JobItem[] = [];
  private candidates: CandidateItem[] = [];
  private attendance: AttendanceItem[] = [];
  private leaves: LeaveItem[] = [];
  private payrolls: PayrollItem[] = [];
  private performances: PerformanceItem[] = [];
  private interviews: InterviewItem[] = [];
  private trainings: TrainingItem[] = [];
  private documents: DocumentItem[] = [];
  private users: UserItem[] = [];
  private activities: ActivityItem[] = [];

  constructor(private http: HttpClient) {
    this.initData();
  }

  private initData(): void {
    const raw = localStorage.getItem(this.STORAGE_KEY);
    if (raw) {
      try {
        const parsed = JSON.parse(raw);
        this.employees = parsed.employees || [];
        this.departments = parsed.departments || [];
        this.jobs = parsed.jobs || [];
        this.candidates = parsed.candidates || [];
        this.attendance = parsed.attendance || [];
        this.leaves = parsed.leaves || [];
        this.payrolls = parsed.payrolls || [];
        this.performances = parsed.performances || [];
        this.interviews = parsed.interviews || [];
        this.trainings = parsed.trainings || [];
        this.documents = parsed.documents || [];
        this.users = parsed.users || [];
        this.activities = parsed.activities || [];
      } catch (e) {
        console.error('Error parsing localStorage, loading defaults', e);
        this.seedDefaultData();
      }
    } else {
      this.seedDefaultData();
      this.saveToStorage();
    }

    // Refresh from live backend API asynchronously
    this.fetchAllFromApi();
  }

  public fetchAllFromApi(): void {
    this.fetchDepartments().subscribe();
    this.fetchEmployees().subscribe();
    this.fetchJobs().subscribe();
    this.fetchCandidates().subscribe();
    this.fetchAttendance().subscribe();
    this.fetchLeaves().subscribe();
    this.fetchPayroll().subscribe();
    this.fetchPerformance().subscribe();
    this.fetchInterviews().subscribe();
    this.fetchTrainings().subscribe();
    this.fetchDocuments().subscribe();
    this.fetchUsers().subscribe();
  }

  private saveToStorage(): void {
    try {
      const data = {
        employees: this.employees,
        departments: this.departments,
        jobs: this.jobs,
        candidates: this.candidates,
        attendance: this.attendance,
        leaves: this.leaves,
        payrolls: this.payrolls,
        performances: this.performances,
        interviews: this.interviews,
        trainings: this.trainings,
        documents: this.documents,
        users: this.users,
        activities: this.activities
      };
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(data));
    } catch (e) {
      console.error('Failed to save to localStorage', e);
    }
  }

  private seedDefaultData(): void {
    this.departments = [
      { id: 1, departmentId: 'DEP-101', name: 'Human Resources', code: 'HR-01', description: 'Handles recruitment, people operations, benefits, and labor compliance.', manager: 'Sarah Jenkins', employeeCount: 4 },
      { id: 2, departmentId: 'DEP-102', name: 'Software Engineering', code: 'ENG-02', description: 'Core software engineering, cloud systems, and platform products.', manager: 'David Chen', employeeCount: 12 },
      { id: 3, departmentId: 'DEP-103', name: 'Marketing & Sales', code: 'MKT-03', description: 'Brand expansion, lead generation, digital growth, and client relations.', manager: 'Emily Watson', employeeCount: 6 },
      { id: 4, departmentId: 'DEP-104', name: 'Finance & Accounts', code: 'FIN-04', description: 'Budget planning, accounts payable, fiscal compliance, and tax audits.', manager: 'Robert Miller', employeeCount: 3 },
      { id: 5, departmentId: 'DEP-105', name: 'Operations & IT', code: 'OPS-05', description: 'IT infrastructure support, workstation hardware, logistics and facilities.', manager: 'Michael Chang', employeeCount: 5 }
    ];

    this.jobs = [
      { id: 1, jobId: 'JOB-201', jobTitle: 'Senior Full Stack Engineer', department: 'Software Engineering', description: 'Develop enterprise TypeScript and cloud microservice features.', status: 'Open', location: 'San Francisco, CA (Hybrid)', employmentType: 'Full Time', experienceRequired: '4+ Years', salary: '$95,000 - $115,000' },
      { id: 2, jobId: 'JOB-202', jobTitle: 'HR Specialist', department: 'Human Resources', description: 'Coordinate talent screening, benefits onboarding, and employee engagement.', status: 'Open', location: 'Austin, TX (On-site)', employmentType: 'Full Time', experienceRequired: '2+ Years', salary: '$62,000 - $75,000' },
      { id: 3, jobId: 'JOB-203', jobTitle: 'UI/UX Product Designer', department: 'Software Engineering', description: 'Lead wireframing, high-fidelity prototypes, and user accessibility testing.', status: 'Open', location: 'Remote', employmentType: 'Full Time', experienceRequired: '3+ Years', salary: '$78,000 - $90,000' },
      { id: 4, jobId: 'JOB-204', jobTitle: 'Digital Marketing Strategist', department: 'Marketing & Sales', description: 'Execute multi-channel advertising, organic SEO, and conversion analytics.', status: 'Draft', location: 'New York, NY', employmentType: 'Contract', experienceRequired: '3+ Years', salary: '$65,000 - $80,000' },
      { id: 5, jobId: 'JOB-205', jobTitle: 'Financial Analyst', department: 'Finance & Accounts', description: 'Quarterly forecasting, variance analysis, and cash flow balance sheets.', status: 'Closed', location: 'Chicago, IL', employmentType: 'Full Time', experienceRequired: '2+ Years', salary: '$70,000 - $85,000' }
    ];

    this.employees = [
      { id: 1, empId: 'EMP-001', name: 'Sarah Jenkins', firstName: 'Sarah', lastName: 'Jenkins', email: 'sarah.j@hrgenius.com', phone: '+1 555-0101', department: 'Human Resources', job: 'HR Director', designation: 'HR Director', joiningDate: '2022-03-15', status: 'Active', salary: 85000, gender: 'Female', employmentType: 'Full Time', city: 'Austin', address: '422 Elm Crest Blvd, Austin, TX' },
      { id: 2, empId: 'EMP-002', name: 'David Chen', firstName: 'David', lastName: 'Chen', email: 'david.c@hrgenius.com', phone: '+1 555-0102', department: 'Software Engineering', job: 'Lead Architect', designation: 'Lead Architect', joiningDate: '2021-06-10', status: 'Active', salary: 110000, gender: 'Male', employmentType: 'Full Time', city: 'San Francisco', address: '88 Tech Valley Way, San Francisco, CA' },
      { id: 3, empId: 'EMP-003', name: 'Emily Watson', firstName: 'Emily', lastName: 'Watson', email: 'emily.w@hrgenius.com', phone: '+1 555-0103', department: 'Marketing & Sales', job: 'Marketing Head', designation: 'Marketing Head', joiningDate: '2022-09-01', status: 'Active', salary: 78000, gender: 'Female', employmentType: 'Full Time', city: 'New York', address: '12 Madison Ave, New York, NY' },
      { id: 4, empId: 'EMP-004', name: 'Robert Miller', firstName: 'Robert', lastName: 'Miller', email: 'robert.m@hrgenius.com', phone: '+1 555-0104', department: 'Finance & Accounts', job: 'Finance Manager', designation: 'Finance Manager', joiningDate: '2020-01-20', status: 'Active', salary: 90000, gender: 'Male', employmentType: 'Full Time', city: 'Chicago', address: '500 Michigan Ave, Chicago, IL' },
      { id: 5, empId: 'EMP-005', name: 'Jessica Taylor', firstName: 'Jessica', lastName: 'Taylor', email: 'jessica.t@hrgenius.com', phone: '+1 555-0105', department: 'Software Engineering', job: 'Frontend Developer', designation: 'Frontend Developer', joiningDate: '2023-02-14', status: 'Active', salary: 74000, gender: 'Female', employmentType: 'Full Time', city: 'San Jose', address: '310 Silicon Park, San Jose, CA' },
      { id: 6, empId: 'EMP-006', name: 'Alex Rivera', firstName: 'Alex', lastName: 'Rivera', email: 'alex.r@hrgenius.com', phone: '+1 555-0106', department: 'Operations & IT', job: 'IT Support Specialist', designation: 'IT Support Specialist', joiningDate: '2023-08-11', status: 'On Leave', salary: 58000, gender: 'Male', employmentType: 'Full Time', city: 'Austin', address: '104 Riverbend Rd, Austin, TX' }
    ];

    this.candidates = [
      { id: 1, candidateId: 'CAN-301', name: 'Alex Morgan', firstName: 'Alex', lastName: 'Morgan', email: 'alex.m@gmail.com', phone: '+1 555-0211', appliedJob: 'Senior Full Stack Engineer', status: 'Interviewing', qualification: "Master's in Computer Science", experience: '5 years', resumeUrl: 'https://hrgenius.local/resumes/alex-morgan.pdf', applicationDate: '2026-09-22' },
      { id: 2, candidateId: 'CAN-302', name: 'Chloe Bennett', firstName: 'Chloe', lastName: 'Bennett', email: 'chloe.b@gmail.com', phone: '+1 555-0212', appliedJob: 'UI/UX Product Designer', status: 'Offered', qualification: 'B.Des Interaction Design', experience: '4 years', resumeUrl: 'https://hrgenius.local/resumes/chloe-bennett.pdf', applicationDate: '2026-09-18' },
      { id: 3, candidateId: 'CAN-303', name: 'Daniel Brooks', firstName: 'Daniel', lastName: 'Brooks', email: 'daniel.b@gmail.com', phone: '+1 555-0213', appliedJob: 'HR Specialist', status: 'Applied', qualification: 'B.B.A Human Resources', experience: '2 years', resumeUrl: 'https://hrgenius.local/resumes/daniel-brooks.pdf', applicationDate: '2026-09-28' },
      { id: 4, candidateId: 'CAN-304', name: 'Sophia Patel', firstName: 'Sophia', lastName: 'Patel', email: 'sophia.p@gmail.com', phone: '+1 555-0214', appliedJob: 'Digital Marketing Strategist', status: 'Screening', qualification: 'B.A. Digital Media', experience: '3 years', resumeUrl: 'https://hrgenius.local/resumes/sophia-patel.pdf', applicationDate: '2026-09-27' },
      { id: 5, candidateId: 'CAN-305', name: 'Ryan Cooper', firstName: 'Ryan', lastName: 'Cooper', email: 'ryan.c@gmail.com', phone: '+1 555-0215', appliedJob: 'Senior Full Stack Engineer', status: 'Hired', qualification: 'B.S. Software Engineering', experience: '4 years', resumeUrl: 'https://hrgenius.local/resumes/ryan-cooper.pdf', applicationDate: '2026-09-10' }
    ];

    this.attendance = [
      { id: 1, employee: 'David Chen', employeeId: 'EMP-002', date: '2026-10-01', checkIn: '08:58 AM', checkOut: '05:32 PM', status: 'Present', remarks: 'On time' },
      { id: 2, employee: 'Sarah Jenkins', employeeId: 'EMP-001', date: '2026-10-01', checkIn: '08:52 AM', checkOut: '05:15 PM', status: 'Present', remarks: 'On time' },
      { id: 3, employee: 'Jessica Taylor', employeeId: 'EMP-005', date: '2026-10-01', checkIn: '09:28 AM', checkOut: '06:05 PM', status: 'Late', remarks: 'Metro transit delay' },
      { id: 4, employee: 'Alex Rivera', employeeId: 'EMP-006', date: '2026-10-01', checkIn: '--', checkOut: '--', status: 'Absent', remarks: 'Sick leave approved' },
      { id: 5, employee: 'Emily Watson', employeeId: 'EMP-003', date: '2026-10-01', checkIn: '09:00 AM', checkOut: '01:30 PM', status: 'Half Day', remarks: 'Medical appointment' },
      { id: 6, employee: 'Robert Miller', employeeId: 'EMP-004', date: '2026-10-01', checkIn: '08:48 AM', checkOut: '05:20 PM', status: 'Present', remarks: 'On time' }
    ];

    this.leaves = [
      { id: 1, employee: 'Alex Rivera', employeeId: 'EMP-006', leaveType: 'Sick Leave', startDate: '2026-10-01', endDate: '2026-10-03', totalDays: 3, appliedDate: '2026-09-30', reason: 'Viral infection medical rest', status: 'Pending' },
      { id: 2, employee: 'Emily Watson', employeeId: 'EMP-003', leaveType: 'Casual Leave', startDate: '2026-10-05', endDate: '2026-10-06', totalDays: 2, appliedDate: '2026-09-29', reason: 'Attending family anniversary', status: 'Approved' },
      { id: 3, employee: 'David Chen', employeeId: 'EMP-002', leaveType: 'Earned Leave', startDate: '2026-09-12', endDate: '2026-09-16', totalDays: 5, appliedDate: '2026-09-01', reason: 'Annual holiday vacation', status: 'Approved' },
      { id: 4, employee: 'Jessica Taylor', employeeId: 'EMP-005', leaveType: 'Emergency Leave', startDate: '2026-09-24', endDate: '2026-09-24', totalDays: 1, appliedDate: '2026-09-24', reason: 'Plumbing home emergency', status: 'Approved' }
    ];

    this.payrolls = [
      { id: 1, employee: 'David Chen', employeeId: 'EMP-002', salary: 9166, allowances: 800, deductions: 600, bonus: 1000, netSalary: 10366, paymentDate: '2026-09-30', payMonth: 'September 2026', status: 'Paid' },
      { id: 2, employee: 'Sarah Jenkins', employeeId: 'EMP-001', salary: 7083, allowances: 650, deductions: 450, bonus: 500, netSalary: 7783, paymentDate: '2026-09-30', payMonth: 'September 2026', status: 'Paid' },
      { id: 3, employee: 'Emily Watson', employeeId: 'EMP-003', salary: 6500, allowances: 500, deductions: 400, bonus: 400, netSalary: 7000, paymentDate: '2026-09-30', payMonth: 'September 2026', status: 'Paid' },
      { id: 4, employee: 'Robert Miller', employeeId: 'EMP-004', salary: 7500, allowances: 600, deductions: 500, bonus: 500, netSalary: 8100, paymentDate: '2026-09-30', payMonth: 'September 2026', status: 'Paid' },
      { id: 5, employee: 'Jessica Taylor', employeeId: 'EMP-005', salary: 6166, allowances: 400, deductions: 350, bonus: 300, netSalary: 6516, paymentDate: '2026-09-30', payMonth: 'September 2026', status: 'Paid' },
      { id: 6, employee: 'Alex Rivera', employeeId: 'EMP-006', salary: 4833, allowances: 300, deductions: 280, bonus: 200, netSalary: 5053, paymentDate: '2026-10-05', payMonth: 'September 2026', status: 'Pending' }
    ];

    this.performances = [
      { id: 1, employee: 'David Chen', employeeId: 'EMP-002', reviewDate: '2026-09-25', rating: 5, goals: 'Architect real-time streaming pipelines', achievements: 'Reduced service latency by 42% across core endpoints', feedback: 'Exceptional technical vision and collaborative leadership across team.' },
      { id: 2, employee: 'Jessica Taylor', employeeId: 'EMP-005', reviewDate: '2026-09-20', rating: 4, goals: 'Revamp design tokens and component accessibility', achievements: 'Delivered WCAG 2.1 AA compliant UI library 1 week early', feedback: 'Outstanding attention to user experience details and high work quality.' },
      { id: 3, employee: 'Sarah Jenkins', employeeId: 'EMP-001', reviewDate: '2026-09-18', rating: 5, goals: 'Implement streamlined applicant tracking workflow', achievements: 'Shortened average time-to-hire from 42 days to 24 days', feedback: 'Strategic HR partner who excels at talent retention and team culture.' },
      { id: 4, employee: 'Emily Watson', employeeId: 'EMP-003', reviewDate: '2026-09-15', rating: 4, goals: 'Drive 25% growth in qualified pipeline leads', achievements: 'Achieved 31% Q3 expansion with optimized webinar campaigns', feedback: 'Creative, metrics-driven approach to inbound marketing growth.' }
    ];

    this.interviews = [
      { id: 1, candidate: 'Alex Morgan', job: 'Senior Full Stack Engineer', interviewDate: '2026-10-03 14:00', interviewer: 'David Chen', mode: 'Online', status: 'Scheduled', feedback: 'System architecture and coding challenge evaluation.' },
      { id: 2, candidate: 'Chloe Bennett', job: 'UI/UX Product Designer', interviewDate: '2026-09-28 11:30', interviewer: 'Sarah Jenkins & Emily Watson', mode: 'In-Person', status: 'Completed', feedback: 'Stunning portfolio presentation. High aesthetic standards and strong user empathy.' },
      { id: 3, candidate: 'Daniel Brooks', job: 'HR Specialist', interviewDate: '2026-10-04 10:00', interviewer: 'Sarah Jenkins', mode: 'Online', status: 'Scheduled', feedback: 'HR policy review and situational behavioral questions.' },
      { id: 4, candidate: 'Sophia Patel', job: 'Digital Marketing Strategist', interviewDate: '2026-10-02 15:30', interviewer: 'Emily Watson', mode: 'In-Person', status: 'Scheduled', feedback: 'Campaign case study presentation and performance metrics review.' }
    ];

    this.trainings = [
      { id: 1, employee: 'Jessica Taylor', trainingName: 'Cloud Solutions Architecture', trainer: 'AWS Learning Team', startDate: '2026-10-10', endDate: '2026-10-24', status: 'Upcoming' },
      { id: 2, employee: 'David Chen', trainingName: 'Advanced Microservices Security & Zero Trust', trainer: 'CyberSec Global Institute', startDate: '2026-09-01', endDate: '2026-09-28', status: 'Completed' },
      { id: 3, employee: 'Sarah Jenkins', trainingName: 'Modern People Leadership & DEI Strategies', trainer: 'SHRM Executive Faculty', startDate: '2026-09-15', endDate: '2026-10-15', status: 'In Progress' },
      { id: 4, employee: 'Emily Watson', trainingName: 'AI-Powered Growth Marketing & SEO', trainer: 'HubSpot Academy Certified', startDate: '2026-10-12', endDate: '2026-10-26', status: 'Upcoming' }
    ];

    this.documents = [
      { id: 1, employee: 'David Chen', documentName: 'Executive Employment Agreement', documentType: 'Contract', documentUrl: '/documents/david_chen_contract.pdf', uploadDate: '2021-06-10', status: 'Verified' },
      { id: 2, employee: 'Sarah Jenkins', documentName: 'Official Passport & ID Verification', documentType: 'ID Proof', documentUrl: '/documents/sarah_id_proof.pdf', uploadDate: '2022-03-15', status: 'Verified' },
      { id: 3, employee: 'Jessica Taylor', documentName: 'B.S. Computer Science Degree Certificate', documentType: 'Certificate', documentUrl: '/documents/jessica_degree.pdf', uploadDate: '2023-02-14', status: 'Verified' },
      { id: 4, employee: 'Alex Rivera', documentName: 'Medical Leave Physician Certificate', documentType: 'Policy', documentUrl: '/documents/alex_medical_cert.pdf', uploadDate: '2026-10-01', status: 'Pending' }
    ];

    this.users = [
      { id: 1, username: 'admin', employee: 'Sarah Jenkins', role: 'Admin', status: 'Active', email: 'admin@hrgenius.com' },
      { id: 2, username: 'david_lead', employee: 'David Chen', role: 'HR Manager', status: 'Active', email: 'david.c@hrgenius.com' },
      { id: 3, username: 'emily_mkt', employee: 'Emily Watson', role: 'Recruiter', status: 'Active', email: 'emily.w@hrgenius.com' },
      { id: 4, username: 'jessica_dev', employee: 'Jessica Taylor', role: 'Employee', status: 'Active', email: 'jessica.t@hrgenius.com' },
      { id: 5, username: 'demo_user', employee: 'Alex Rivera', role: 'Employee', status: 'Inactive', email: 'demo@hrgenius.com' }
    ];

    this.activities = [
      { id: 1, action: 'Candidate Applied', target: 'Alex Morgan for Senior Full Stack Engineer', time: '10 mins ago', user: 'Sarah Jenkins', type: 'candidate' },
      { id: 2, action: 'Leave Requested', target: 'Alex Rivera applied for 3 days Sick Leave', time: '45 mins ago', user: 'Alex Rivera', type: 'leave' },
      { id: 3, action: 'Attendance Marked', target: 'Jessica Taylor checked in at 09:28 AM', time: '1 hour ago', user: 'Jessica Taylor', type: 'employee' },
      { id: 4, action: 'Payroll Generated', target: 'September 2026 cycle for 6 team members', time: 'Yesterday', user: 'Robert Miller', type: 'payroll' },
      { id: 5, action: 'Training Completed', target: 'David Chen completed Zero Trust Security course', time: '2 days ago', user: 'David Chen', type: 'training' }
    ];
  }

  // Dashboard Stats
  getDashboardStats(): DashboardStats {
    const totalEmployees = this.employees.length;
    const totalDepartments = this.departments.length;
    const totalCandidates = this.candidates.length;
    const presentToday = this.attendance.filter(a => a.status === 'Present' || a.status === 'Late').length;
    const pendingLeaves = this.leaves.filter(l => l.status === 'Pending').length;
    const totalPayroll = this.payrolls.reduce((sum, p) => sum + (p.netSalary || 0), 0);
    const avgSalary = totalEmployees > 0 ? Math.round(this.employees.reduce((sum, e) => sum + (Number(e.salary) || 0), 0) / totalEmployees) : 0;
    const openJobs = this.jobs.filter(j => j.status === 'Open').length;

    return {
      totalEmployees,
      totalDepartments,
      totalCandidates,
      presentToday,
      pendingLeaves,
      totalPayroll,
      avgSalary,
      openJobs
    };
  }

  fetchDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/dashboard/stats`).pipe(
      catchError(() => of(this.getDashboardStats()))
    );
  }

  // Employees
  getEmployees(): EmployeeItem[] {
    return [...this.employees];
  }

  fetchEmployees(): Observable<EmployeeItem[]> {
    return this.http.get<EmployeeItem[]>(`${this.apiUrl}/employees`).pipe(
      tap(res => {
        if (res && res.length) {
          this.employees = res;
          this.saveToStorage();
        }
      }),
      catchError(() => of(this.employees))
    );
  }

  addEmployee(emp: Partial<EmployeeItem>): EmployeeItem {
    const id = this.employees.length > 0 ? Math.max(...this.employees.map(e => e.id)) + 1 : 1;
    const empId = emp.empId || `EMP-${String(id).padStart(3, '0')}`;
    const name = emp.name || `${emp.firstName || ''} ${emp.lastName || ''}`.trim() || 'New Employee';

    let matchedDeptName = emp.department || 'Software Engineering';
    let matchedDeptId = emp.departmentId;
    if (emp.department) {
      const foundDept = this.departments.find(d =>
        d.name?.toLowerCase() === emp.department?.toLowerCase() ||
        d.departmentId?.toLowerCase() === emp.department?.toLowerCase()
      );
      if (foundDept) {
        matchedDeptName = foundDept.name;
        matchedDeptId = foundDept.id;
      }
    } else if (matchedDeptId) {
      const foundDept = this.departments.find(d => d.id === matchedDeptId);
      if (foundDept) {
        matchedDeptName = foundDept.name;
      }
    }

    const newEmp: EmployeeItem = {
      id,
      empId,
      name,
      firstName: emp.firstName || name.split(' ')[0] || '',
      lastName: emp.lastName || name.split(' ').slice(1).join(' ') || '',
      email: emp.email || `${empId.toLowerCase()}@hrgenius.com`,
      phone: emp.phone || '+1 555-0000',
      department: matchedDeptName,
      departmentId: matchedDeptId,
      job: emp.job || emp.designation || 'Staff Associate',
      designation: emp.designation || emp.job || 'Staff Associate',
      joiningDate: emp.joiningDate || new Date().toISOString().split('T')[0],
      status: emp.status || 'Active',
      salary: emp.salary || 65000,
      gender: emp.gender || 'Not Specified',
      employmentType: emp.employmentType || 'Full Time',
      city: emp.city || 'Austin',
      address: emp.address || 'Company HQ'
    };
    this.employees.unshift(newEmp);
    this.addActivity('Added Employee', `${newEmp.name} (${newEmp.department})`, 'employee');
    this.saveToStorage();

    // Live API Call
    this.http.post<EmployeeItem>(`${this.apiUrl}/employees`, {
      ...newEmp,
      department: matchedDeptName,
      departmentId: matchedDeptId
    }).subscribe({
      next: (created) => {
        if (created && created.id) {
          const idx = this.employees.findIndex(e => e.id === id || e.empId === newEmp.empId);
          if (idx !== -1) {
            this.employees[idx] = { ...this.employees[idx], ...created };
            this.saveToStorage();
          }
        }
      },
      error: (e) => console.warn('Could not sync employee to backend', e)
    });

    return newEmp;
  }

  updateEmployee(id: number, emp: Partial<EmployeeItem>): EmployeeItem | null {
    const index = this.employees.findIndex(e => e.id === id);
    if (index === -1) return null;

    let matchedDeptName = emp.department;
    let matchedDeptId = emp.departmentId;

    if (emp.department) {
      const foundDept = this.departments.find(d =>
        d.name?.toLowerCase() === emp.department?.toLowerCase() ||
        d.departmentId?.toLowerCase() === emp.department?.toLowerCase()
      );
      if (foundDept) {
        matchedDeptName = foundDept.name;
        matchedDeptId = foundDept.id;
      }
    } else if (matchedDeptId) {
      const foundDept = this.departments.find(d => d.id === matchedDeptId);
      if (foundDept) {
        matchedDeptName = foundDept.name;
      }
    }

    const name = emp.name || (emp.firstName || emp.lastName ? `${emp.firstName || ''} ${emp.lastName || ''}`.trim() : this.employees[index].name);
    this.employees[index] = {
      ...this.employees[index],
      ...emp,
      name,
      department: matchedDeptName || this.employees[index].department,
      departmentId: matchedDeptId !== undefined ? matchedDeptId : this.employees[index].departmentId,
      designation: emp.designation || emp.job || this.employees[index].designation,
      job: emp.job || emp.designation || this.employees[index].job
    };
    this.addActivity('Updated Employee', `${this.employees[index].name}`, 'employee');
    this.saveToStorage();

    // Live API Call
    const payload = {
      ...this.employees[index],
      department: this.employees[index].department,
      departmentId: this.employees[index].departmentId
    };

    this.http.put<EmployeeItem>(`${this.apiUrl}/employees/${id}`, payload).subscribe({
      next: (updated) => {
        if (updated) {
          const idx = this.employees.findIndex(e => e.id === id);
          if (idx !== -1) {
            this.employees[idx] = { ...this.employees[idx], ...updated };
            this.saveToStorage();
          }
        }
      },
      error: (e) => console.warn('Could not update employee in backend', e)
    });

    return this.employees[index];
  }

  deleteEmployee(id: number): boolean {
    const emp = this.employees.find(e => e.id === id);
    this.employees = this.employees.filter(e => e.id !== id);
    if (emp) {
      this.addActivity('Deleted Employee', `${emp.name}`, 'employee');
    }
    this.saveToStorage();

    // Live API Call
    this.http.delete(`${this.apiUrl}/employees/${id}`).subscribe({
      error: (e) => console.warn('Could not delete employee from backend', e)
    });

    return true;
  }

  // Departments
  getDepartments(): DepartmentItem[] {
    return [...this.departments];
  }

  fetchDepartments(): Observable<DepartmentItem[]> {
    return this.http.get<DepartmentItem[]>(`${this.apiUrl}/departments`).pipe(
      tap(res => {
        if (res && res.length) {
          this.departments = res;
          this.saveToStorage();
        }
      }),
      catchError(() => of(this.departments))
    );
  }

  addDepartment(dep: Partial<DepartmentItem>): DepartmentItem {
    const id = this.departments.length > 0 ? Math.max(...this.departments.map(d => d.id)) + 1 : 1;
    const newDep: DepartmentItem = {
      id,
      departmentId: dep.departmentId || `DEP-${id + 100}`,
      name: dep.name || 'New Department',
      code: dep.code || `D-${id}`,
      description: dep.description || 'Department responsibilities and operations.',
      manager: dep.manager || 'Sarah Jenkins',
      employeeCount: dep.employeeCount || 0
    };
    this.departments.push(newDep);
    this.saveToStorage();

    this.http.post<DepartmentItem>(`${this.apiUrl}/departments`, newDep).subscribe({
      next: (created) => {
        if (created && created.id) {
          const idx = this.departments.findIndex(d => d.id === id);
          if (idx !== -1) {
            this.departments[idx] = { ...this.departments[idx], ...created };
            this.saveToStorage();
          }
        }
      },
      error: (e) => console.warn('Could not save department to backend', e)
    });

    return newDep;
  }

  updateDepartment(id: number, dep: Partial<DepartmentItem>): DepartmentItem | null {
    const idx = this.departments.findIndex(d => d.id === id);
    if (idx === -1) return null;
    this.departments[idx] = { ...this.departments[idx], ...dep };
    this.saveToStorage();

    this.http.put<DepartmentItem>(`${this.apiUrl}/departments/${id}`, this.departments[idx]).subscribe({
      error: (e) => console.warn('Could not update department in backend', e)
    });

    return this.departments[idx];
  }

  deleteDepartment(id: number): boolean {
    this.departments = this.departments.filter(d => d.id !== id);
    this.saveToStorage();

    this.http.delete(`${this.apiUrl}/departments/${id}`).subscribe({
      error: (e) => console.warn('Could not delete department from backend', e)
    });

    return true;
  }

  // Jobs
  getJobs(): JobItem[] {
    return [...this.jobs];
  }

  fetchJobs(): Observable<JobItem[]> {
    return this.http.get<JobItem[]>(`${this.apiUrl}/jobs`).pipe(
      tap(res => {
        if (res && res.length) {
          this.jobs = res;
          this.saveToStorage();
        }
      }),
      catchError(() => of(this.jobs))
    );
  }

  addJob(job: Partial<JobItem>): JobItem {
    const id = this.jobs.length > 0 ? Math.max(...this.jobs.map(j => j.id)) + 1 : 1;
    const newJob: JobItem = {
      id,
      jobId: job.jobId || `JOB-${id + 200}`,
      jobTitle: job.jobTitle || 'New Position',
      department: job.department || 'Software Engineering',
      description: job.description || 'Role duties and requirements.',
      status: job.status || 'Open',
      location: job.location || 'Headquarters',
      employmentType: job.employmentType || 'Full Time',
      experienceRequired: job.experienceRequired || '1-3 Years',
      salary: job.salary || '$60,000 - $80,000'
    };
    this.jobs.push(newJob);
    this.saveToStorage();

    this.http.post<JobItem>(`${this.apiUrl}/jobs`, newJob).subscribe({
      next: (created) => {
        if (created && created.id) {
          const idx = this.jobs.findIndex(j => j.id === id);
          if (idx !== -1) {
            this.jobs[idx] = { ...this.jobs[idx], ...created };
            this.saveToStorage();
          }
        }
      },
      error: (e) => console.warn('Could not save job to backend', e)
    });

    return newJob;
  }

  updateJob(id: number, job: Partial<JobItem>): JobItem | null {
    const idx = this.jobs.findIndex(j => j.id === id);
    if (idx === -1) return null;
    this.jobs[idx] = { ...this.jobs[idx], ...job };
    this.saveToStorage();

    this.http.put<JobItem>(`${this.apiUrl}/jobs/${id}`, this.jobs[idx]).subscribe({
      error: (e) => console.warn('Could not update job in backend', e)
    });

    return this.jobs[idx];
  }

  deleteJob(id: number): boolean {
    this.jobs = this.jobs.filter(j => j.id !== id);
    this.saveToStorage();

    this.http.delete(`${this.apiUrl}/jobs/${id}`).subscribe({
      error: (e) => console.warn('Could not delete job from backend', e)
    });

    return true;
  }

  // Candidates
  getCandidates(): CandidateItem[] {
    return [...this.candidates];
  }

  fetchCandidates(): Observable<CandidateItem[]> {
    return this.http.get<CandidateItem[]>(`${this.apiUrl}/candidates`).pipe(
      tap(res => {
        if (res && res.length) {
          this.candidates = res;
          this.saveToStorage();
        }
      }),
      catchError(() => of(this.candidates))
    );
  }

  addCandidate(can: Partial<CandidateItem>): CandidateItem {
    const id = this.candidates.length > 0 ? Math.max(...this.candidates.map(c => c.id)) + 1 : 1;
    const name = can.name || `${can.firstName || ''} ${can.lastName || ''}`.trim() || 'New Candidate';
    const newCan: CandidateItem = {
      id,
      candidateId: can.candidateId || `CAN-${id + 300}`,
      name,
      firstName: can.firstName || name.split(' ')[0] || '',
      lastName: can.lastName || name.split(' ').slice(1).join(' ') || '',
      email: can.email || 'candidate@example.com',
      phone: can.phone || '+1 555-0199',
      appliedJob: can.appliedJob || 'Senior Full Stack Engineer',
      status: can.status || 'Applied',
      qualification: can.qualification || "Bachelor's Degree",
      experience: can.experience || '2+ years',
      resumeUrl: can.resumeUrl || 'https://hrgenius.local/resumes/candidate.pdf',
      applicationDate: can.applicationDate || new Date().toISOString().split('T')[0]
    };
    this.candidates.push(newCan);
    this.addActivity('Candidate Applied', `${newCan.name} for ${newCan.appliedJob}`, 'candidate');
    this.saveToStorage();

    this.http.post<CandidateItem>(`${this.apiUrl}/candidates`, newCan).subscribe({
      next: (created) => {
        if (created && created.id) {
          const idx = this.candidates.findIndex(c => c.id === id);
          if (idx !== -1) {
            this.candidates[idx] = { ...this.candidates[idx], ...created };
            this.saveToStorage();
          }
        }
      },
      error: (e) => console.warn('Could not save candidate to backend', e)
    });

    return newCan;
  }

  updateCandidate(id: number, can: Partial<CandidateItem>): CandidateItem | null {
    const idx = this.candidates.findIndex(c => c.id === id);
    if (idx === -1) return null;
    this.candidates[idx] = { ...this.candidates[idx], ...can };
    this.saveToStorage();

    this.http.put<CandidateItem>(`${this.apiUrl}/candidates/${id}`, this.candidates[idx]).subscribe({
      error: (e) => console.warn('Could not update candidate in backend', e)
    });

    return this.candidates[idx];
  }

  deleteCandidate(id: number): boolean {
    this.candidates = this.candidates.filter(c => c.id !== id);
    this.saveToStorage();

    this.http.delete(`${this.apiUrl}/candidates/${id}`).subscribe({
      error: (e) => console.warn('Could not delete candidate from backend', e)
    });

    return true;
  }

  // Attendance
  getAttendance(): AttendanceItem[] {
    return [...this.attendance];
  }

  fetchAttendance(): Observable<AttendanceItem[]> {
    return this.http.get<AttendanceItem[]>(`${this.apiUrl}/attendance`).pipe(
      tap(res => {
        if (res && res.length) {
          this.attendance = res;
          this.saveToStorage();
        }
      }),
      catchError(() => of(this.attendance))
    );
  }

  addAttendance(att: Partial<AttendanceItem>): AttendanceItem {
    const id = this.attendance.length > 0 ? Math.max(...this.attendance.map(a => a.id)) + 1 : 1;
    const newAtt: AttendanceItem = {
      id,
      employee: att.employee || 'Sarah Jenkins',
      employeeId: att.employeeId || 'EMP-001',
      date: att.date || new Date().toISOString().split('T')[0],
      checkIn: att.checkIn || '09:00 AM',
      checkOut: att.checkOut || '05:30 PM',
      status: att.status || 'Present',
      remarks: att.remarks || 'Standard log'
    };
    this.attendance.unshift(newAtt);
    this.addActivity('Attendance Logged', `${newAtt.employee} marked as ${newAtt.status}`, 'employee');
    this.saveToStorage();

    this.http.post<AttendanceItem>(`${this.apiUrl}/attendance`, newAtt).subscribe({
      next: (created) => {
        if (created && created.id) {
          const idx = this.attendance.findIndex(a => a.id === id);
          if (idx !== -1) {
            this.attendance[idx] = { ...this.attendance[idx], ...created };
            this.saveToStorage();
          }
        }
      },
      error: (e) => console.warn('Could not save attendance to backend', e)
    });

    return newAtt;
  }

  updateAttendance(id: number, att: Partial<AttendanceItem>): AttendanceItem | null {
    const idx = this.attendance.findIndex(a => a.id === id);
    if (idx === -1) return null;
    this.attendance[idx] = { ...this.attendance[idx], ...att };
    this.saveToStorage();

    this.http.put<AttendanceItem>(`${this.apiUrl}/attendance/${id}`, this.attendance[idx]).subscribe({
      error: (e) => console.warn('Could not update attendance in backend', e)
    });

    return this.attendance[idx];
  }

  deleteAttendance(id: number): boolean {
    this.attendance = this.attendance.filter(a => a.id !== id);
    this.saveToStorage();

    this.http.delete(`${this.apiUrl}/attendance/${id}`).subscribe({
      error: (e) => console.warn('Could not delete attendance from backend', e)
    });

    return true;
  }

  // Leaves
  getLeaves(): LeaveItem[] {
    return [...this.leaves];
  }

  fetchLeaves(): Observable<LeaveItem[]> {
    return this.http.get<LeaveItem[]>(`${this.apiUrl}/leaves`).pipe(
      tap(res => {
        if (res && res.length) {
          this.leaves = res;
          this.saveToStorage();
        }
      }),
      catchError(() => of(this.leaves))
    );
  }

  addLeave(leave: Partial<LeaveItem>): LeaveItem {
    const id = this.leaves.length > 0 ? Math.max(...this.leaves.map(l => l.id)) + 1 : 1;
    const newLeave: LeaveItem = {
      id,
      employee: leave.employee || 'David Chen',
      employeeId: leave.employeeId || 'EMP-002',
      leaveType: leave.leaveType || 'Casual Leave',
      startDate: leave.startDate || new Date().toISOString().split('T')[0],
      endDate: leave.endDate || new Date().toISOString().split('T')[0],
      totalDays: Number(leave.totalDays) || 1,
      appliedDate: leave.appliedDate || new Date().toISOString().split('T')[0],
      reason: leave.reason || 'Personal time off',
      status: leave.status || 'Pending'
    };
    this.leaves.unshift(newLeave);
    this.addActivity('Leave Submitted', `${newLeave.employee} - ${newLeave.leaveType}`, 'leave');
    this.saveToStorage();

    this.http.post<LeaveItem>(`${this.apiUrl}/leaves`, newLeave).subscribe({
      next: (created) => {
        if (created && created.id) {
          const idx = this.leaves.findIndex(l => l.id === id);
          if (idx !== -1) {
            this.leaves[idx] = { ...this.leaves[idx], ...created };
            this.saveToStorage();
          }
        }
      },
      error: (e) => console.warn('Could not save leave to backend', e)
    });

    return newLeave;
  }

  updateLeave(id: number, leave: Partial<LeaveItem>): LeaveItem | null {
    const idx = this.leaves.findIndex(l => l.id === id);
    if (idx === -1) return null;
    this.leaves[idx] = { ...this.leaves[idx], ...leave };
    this.saveToStorage();

    this.http.put<LeaveItem>(`${this.apiUrl}/leaves/${id}`, this.leaves[idx]).subscribe({
      error: (e) => console.warn('Could not update leave in backend', e)
    });

    return this.leaves[idx];
  }

  deleteLeave(id: number): boolean {
    this.leaves = this.leaves.filter(l => l.id !== id);
    this.saveToStorage();

    this.http.delete(`${this.apiUrl}/leaves/${id}`).subscribe({
      error: (e) => console.warn('Could not delete leave from backend', e)
    });

    return true;
  }

  // Payroll
  getPayroll(): PayrollItem[] {
    return [...this.payrolls];
  }

  fetchPayroll(): Observable<PayrollItem[]> {
    return this.http.get<PayrollItem[]>(`${this.apiUrl}/payroll`).pipe(
      tap(res => {
        if (res && res.length) {
          this.payrolls = res;
          this.saveToStorage();
        }
      }),
      catchError(() => of(this.payrolls))
    );
  }

  addPayroll(pay: Partial<PayrollItem>): PayrollItem {
    const id = this.payrolls.length > 0 ? Math.max(...this.payrolls.map(p => p.id)) + 1 : 1;
    const salary = Number(pay.salary) || 5000;
    const allowances = Number(pay.allowances) || 0;
    const deductions = Number(pay.deductions) || 0;
    const bonus = Number(pay.bonus) || 0;
    const netSalary = salary + allowances + bonus - deductions;

    const newPay: PayrollItem = {
      id,
      employee: pay.employee || 'David Chen',
      employeeId: pay.employeeId || 'EMP-002',
      salary,
      allowances,
      deductions,
      bonus,
      netSalary,
      paymentDate: pay.paymentDate || new Date().toISOString().split('T')[0],
      payMonth: pay.payMonth || 'October 2026',
      status: pay.status || 'Paid'
    };
    this.payrolls.unshift(newPay);
    this.addActivity('Payroll Processed', `$${newPay.netSalary} for ${newPay.employee}`, 'payroll');
    this.saveToStorage();

    this.http.post<PayrollItem>(`${this.apiUrl}/payroll`, newPay).subscribe({
      next: (created) => {
        if (created && created.id) {
          const idx = this.payrolls.findIndex(p => p.id === id);
          if (idx !== -1) {
            this.payrolls[idx] = { ...this.payrolls[idx], ...created };
            this.saveToStorage();
          }
        }
      },
      error: (e) => console.warn('Could not save payroll to backend', e)
    });

    return newPay;
  }

  updatePayroll(id: number, pay: Partial<PayrollItem>): PayrollItem | null {
    const idx = this.payrolls.findIndex(p => p.id === id);
    if (idx === -1) return null;
    const salary = pay.salary !== undefined ? Number(pay.salary) : this.payrolls[idx].salary;
    const allowances = pay.allowances !== undefined ? Number(pay.allowances) : this.payrolls[idx].allowances;
    const deductions = pay.deductions !== undefined ? Number(pay.deductions) : this.payrolls[idx].deductions;
    const bonus = pay.bonus !== undefined ? Number(pay.bonus) : this.payrolls[idx].bonus;
    const netSalary = salary + allowances + bonus - deductions;

    this.payrolls[idx] = {
      ...this.payrolls[idx],
      ...pay,
      salary,
      allowances,
      deductions,
      bonus,
      netSalary
    };
    this.saveToStorage();

    this.http.put<PayrollItem>(`${this.apiUrl}/payroll/${id}`, this.payrolls[idx]).subscribe({
      error: (e) => console.warn('Could not update payroll in backend', e)
    });

    return this.payrolls[idx];
  }

  deletePayroll(id: number): boolean {
    this.payrolls = this.payrolls.filter(p => p.id !== id);
    this.saveToStorage();

    this.http.delete(`${this.apiUrl}/payroll/${id}`).subscribe({
      error: (e) => console.warn('Could not delete payroll from backend', e)
    });

    return true;
  }

  // Performance
  getPerformance(): PerformanceItem[] {
    return [...this.performances];
  }

  fetchPerformance(): Observable<PerformanceItem[]> {
    return this.http.get<PerformanceItem[]>(`${this.apiUrl}/performance`).pipe(
      tap(res => {
        if (res && res.length) {
          this.performances = res;
          this.saveToStorage();
        }
      }),
      catchError(() => of(this.performances))
    );
  }

  addPerformance(perf: Partial<PerformanceItem>): PerformanceItem {
    const id = this.performances.length > 0 ? Math.max(...this.performances.map(p => p.id)) + 1 : 1;
    const newPerf: PerformanceItem = {
      id,
      employee: perf.employee || 'Jessica Taylor',
      employeeId: perf.employeeId || 'EMP-005',
      reviewDate: perf.reviewDate || new Date().toISOString().split('T')[0],
      rating: Number(perf.rating) || 5,
      goals: perf.goals || 'Q4 Key Performance Indicators',
      achievements: perf.achievements || 'Exceeded sprint deliverables',
      feedback: perf.feedback || 'Consistently delivers high quality work.'
    };
    this.performances.unshift(newPerf);
    this.saveToStorage();

    this.http.post<PerformanceItem>(`${this.apiUrl}/performance`, newPerf).subscribe({
      next: (created) => {
        if (created && created.id) {
          const idx = this.performances.findIndex(p => p.id === id);
          if (idx !== -1) {
            this.performances[idx] = { ...this.performances[idx], ...created };
            this.saveToStorage();
          }
        }
      },
      error: (e) => console.warn('Could not save performance review to backend', e)
    });

    return newPerf;
  }

  updatePerformance(id: number, perf: Partial<PerformanceItem>): PerformanceItem | null {
    const idx = this.performances.findIndex(p => p.id === id);
    if (idx === -1) return null;
    this.performances[idx] = { ...this.performances[idx], ...perf, rating: Number(perf.rating || this.performances[idx].rating) };
    this.saveToStorage();

    this.http.put<PerformanceItem>(`${this.apiUrl}/performance/${id}`, this.performances[idx]).subscribe({
      error: (e) => console.warn('Could not update performance in backend', e)
    });

    return this.performances[idx];
  }

  deletePerformance(id: number): boolean {
    this.performances = this.performances.filter(p => p.id !== id);
    this.saveToStorage();

    this.http.delete(`${this.apiUrl}/performance/${id}`).subscribe({
      error: (e) => console.warn('Could not delete performance from backend', e)
    });

    return true;
  }

  // Interviews
  getInterviews(): InterviewItem[] {
    return [...this.interviews];
  }

  fetchInterviews(): Observable<InterviewItem[]> {
    return this.http.get<InterviewItem[]>(`${this.apiUrl}/interviews`).pipe(
      tap(res => {
        if (res && res.length) {
          this.interviews = res;
          this.saveToStorage();
        }
      }),
      catchError(() => of(this.interviews))
    );
  }

  addInterview(int: Partial<InterviewItem>): InterviewItem {
    const id = this.interviews.length > 0 ? Math.max(...this.interviews.map(i => i.id)) + 1 : 1;
    const newInt: InterviewItem = {
      id,
      candidate: int.candidate || 'Alex Morgan',
      job: int.job || 'Senior Full Stack Engineer',
      interviewDate: int.interviewDate || `${new Date().toISOString().split('T')[0]} 11:00`,
      interviewer: int.interviewer || 'David Chen',
      mode: int.mode || 'Online',
      status: int.status || 'Scheduled',
      feedback: int.feedback || 'Initial screening and technical assessment.'
    };
    this.interviews.unshift(newInt);
    this.saveToStorage();

    this.http.post<InterviewItem>(`${this.apiUrl}/interviews`, newInt).subscribe({
      next: (created) => {
        if (created && created.id) {
          const idx = this.interviews.findIndex(i => i.id === id);
          if (idx !== -1) {
            this.interviews[idx] = { ...this.interviews[idx], ...created };
            this.saveToStorage();
          }
        }
      },
      error: (e) => console.warn('Could not save interview to backend', e)
    });

    return newInt;
  }

  updateInterview(id: number, int: Partial<InterviewItem>): InterviewItem | null {
    const idx = this.interviews.findIndex(i => i.id === id);
    if (idx === -1) return null;
    this.interviews[idx] = { ...this.interviews[idx], ...int };
    this.saveToStorage();

    this.http.put<InterviewItem>(`${this.apiUrl}/interviews/${id}`, this.interviews[idx]).subscribe({
      error: (e) => console.warn('Could not update interview in backend', e)
    });

    return this.interviews[idx];
  }

  deleteInterview(id: number): boolean {
    this.interviews = this.interviews.filter(i => i.id !== id);
    this.saveToStorage();

    this.http.delete(`${this.apiUrl}/interviews/${id}`).subscribe({
      error: (e) => console.warn('Could not delete interview from backend', e)
    });

    return true;
  }

  // Training
  getTrainings(): TrainingItem[] {
    return [...this.trainings];
  }

  fetchTrainings(): Observable<TrainingItem[]> {
    return this.http.get<TrainingItem[]>(`${this.apiUrl}/training`).pipe(
      tap(res => {
        if (res && res.length) {
          this.trainings = res;
          this.saveToStorage();
        }
      }),
      catchError(() => of(this.trainings))
    );
  }

  addTraining(tr: Partial<TrainingItem>): TrainingItem {
    const id = this.trainings.length > 0 ? Math.max(...this.trainings.map(t => t.id)) + 1 : 1;
    const newTr: TrainingItem = {
      id,
      employee: tr.employee || 'Sarah Jenkins',
      trainingName: tr.trainingName || 'Corporate Leadership Essentials',
      trainer: tr.trainer || 'Senior Learning Advisor',
      startDate: tr.startDate || new Date().toISOString().split('T')[0],
      endDate: tr.endDate || new Date(Date.now() + 14 * 86400000).toISOString().split('T')[0],
      status: tr.status || 'Upcoming'
    };
    this.trainings.unshift(newTr);
    this.saveToStorage();

    this.http.post<TrainingItem>(`${this.apiUrl}/training`, newTr).subscribe({
      next: (created) => {
        if (created && created.id) {
          const idx = this.trainings.findIndex(t => t.id === id);
          if (idx !== -1) {
            this.trainings[idx] = { ...this.trainings[idx], ...created };
            this.saveToStorage();
          }
        }
      },
      error: (e) => console.warn('Could not save training to backend', e)
    });

    return newTr;
  }

  updateTraining(id: number, tr: Partial<TrainingItem>): TrainingItem | null {
    const idx = this.trainings.findIndex(t => t.id === id);
    if (idx === -1) return null;
    this.trainings[idx] = { ...this.trainings[idx], ...tr };
    this.saveToStorage();

    this.http.put<TrainingItem>(`${this.apiUrl}/training/${id}`, this.trainings[idx]).subscribe({
      error: (e) => console.warn('Could not update training in backend', e)
    });

    return this.trainings[idx];
  }

  deleteTraining(id: number): boolean {
    this.trainings = this.trainings.filter(t => t.id !== id);
    this.saveToStorage();

    this.http.delete(`${this.apiUrl}/training/${id}`).subscribe({
      error: (e) => console.warn('Could not delete training from backend', e)
    });

    return true;
  }

  // Documents
  getDocuments(): DocumentItem[] {
    return [...this.documents];
  }

  fetchDocuments(): Observable<DocumentItem[]> {
    return this.http.get<DocumentItem[]>(`${this.apiUrl}/documents`).pipe(
      tap(res => {
        if (res && res.length) {
          this.documents = res;
          this.saveToStorage();
        }
      }),
      catchError(() => of(this.documents))
    );
  }

  addDocument(doc: Partial<DocumentItem>): DocumentItem {
    const id = this.documents.length > 0 ? Math.max(...this.documents.map(d => d.id)) + 1 : 1;
    const newDoc: DocumentItem = {
      id,
      employee: doc.employee || 'David Chen',
      documentName: doc.documentName || 'HR Onboarding Agreement',
      documentType: doc.documentType || 'Contract',
      documentUrl: doc.documentUrl || '/documents/sample_document.pdf',
      uploadDate: doc.uploadDate || new Date().toISOString().split('T')[0],
      status: doc.status || 'Pending'
    };
    this.documents.unshift(newDoc);
    this.saveToStorage();

    this.http.post<DocumentItem>(`${this.apiUrl}/documents`, newDoc).subscribe({
      next: (created) => {
        if (created && created.id) {
          const idx = this.documents.findIndex(d => d.id === id);
          if (idx !== -1) {
            this.documents[idx] = { ...this.documents[idx], ...created };
            this.saveToStorage();
          }
        }
      },
      error: (e) => console.warn('Could not save document to backend', e)
    });

    return newDoc;
  }

  updateDocument(id: number, doc: Partial<DocumentItem>): DocumentItem | null {
    const idx = this.documents.findIndex(d => d.id === id);
    if (idx === -1) return null;
    this.documents[idx] = { ...this.documents[idx], ...doc };
    this.saveToStorage();

    this.http.put<DocumentItem>(`${this.apiUrl}/documents/${id}`, this.documents[idx]).subscribe({
      error: (e) => console.warn('Could not update document in backend', e)
    });

    return this.documents[idx];
  }

  deleteDocument(id: number): boolean {
    this.documents = this.documents.filter(d => d.id !== id);
    this.saveToStorage();

    this.http.delete(`${this.apiUrl}/documents/${id}`).subscribe({
      error: (e) => console.warn('Could not delete document from backend', e)
    });

    return true;
  }

  // Users
  getUsers(): UserItem[] {
    return [...this.users];
  }

  fetchUsers(): Observable<UserItem[]> {
    return this.http.get<UserItem[]>(`${this.apiUrl}/users`).pipe(
      tap(res => {
        if (res && res.length) {
          this.users = res;
          this.saveToStorage();
        }
      }),
      catchError(() => of(this.users))
    );
  }

  addUser(user: Partial<UserItem>): UserItem {
    const id = this.users.length > 0 ? Math.max(...this.users.map(u => u.id)) + 1 : 1;
    const newUser: UserItem = {
      id,
      username: user.username || `user_${id}`,
      employee: user.employee || 'David Chen',
      role: user.role || 'HR Manager',
      status: user.status || 'Active',
      email: user.email || `${user.username || 'user'}@hrgenius.com`
    };
    this.users.unshift(newUser);
    this.saveToStorage();

    this.http.post<UserItem>(`${this.apiUrl}/users`, {
      username: newUser.username,
      employee: newUser.employee,
      email: newUser.email,
      role: newUser.role,
      status: newUser.status,
      password: 'password123'
    }).subscribe({
      next: (created) => {
        if (created && created.id) {
          const idx = this.users.findIndex(u => u.id === id);
          if (idx !== -1) {
            this.users[idx] = { ...this.users[idx], ...created };
            this.saveToStorage();
          }
        }
      },
      error: (e) => console.warn('Could not save user to backend', e)
    });

    return newUser;
  }

  updateUser(id: number, user: Partial<UserItem>): UserItem | null {
    const idx = this.users.findIndex(u => u.id === id);
    if (idx === -1) return null;
    this.users[idx] = { ...this.users[idx], ...user };
    this.saveToStorage();

    this.http.put<UserItem>(`${this.apiUrl}/users/${id}`, this.users[idx]).subscribe({
      error: (e) => console.warn('Could not update user in backend', e)
    });

    return this.users[idx];
  }

  deleteUser(id: number): boolean {
    this.users = this.users.filter(u => u.id !== id);
    this.saveToStorage();

    this.http.delete(`${this.apiUrl}/users/${id}`).subscribe({
      error: (e) => console.warn('Could not delete user from backend', e)
    });

    return true;
  }

  // Activities
  getActivities(): ActivityItem[] {
    return [...this.activities];
  }

  addActivity(action: string, target: string, type: ActivityItem['type']): void {
    const newAct: ActivityItem = {
      id: Date.now(),
      action,
      target,
      time: 'Just now',
      user: 'Sarah Jenkins',
      type
    };
    this.activities.unshift(newAct);
    if (this.activities.length > 25) {
      this.activities.pop();
    }
  }
}
