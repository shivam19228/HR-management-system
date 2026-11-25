import { Routes } from '@angular/router';

import { Layout } from './pages/layout/layout';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';

import { Dashboard } from './pages/dashboard/dashboard';
import { Employees } from './pages/employees/employees';
import { Departments } from './pages/departments/departments';
import { Jobs } from './pages/jobs/jobs';
import { Candidates } from './pages/candidates/candidates';
import { Attendance } from './pages/attendance/attendance';
import { Leaves } from './pages/leaves/leaves';
import { Payroll } from './pages/payroll/payroll';
import { Performance } from './pages/performance/performance';
import { Interviews } from './pages/interviews/interviews';
import { Training } from './pages/training/training';
import { Documents } from './pages/documents/documents';
import { Users } from './pages/users/users';
import { Profile } from './pages/profile/profile';

import { authGuard } from './guards/auth.guard';
import { guestGuard } from './guards/guest.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: Login,
    canActivate: [guestGuard]
  },
  {
    path: 'register',
    component: Register,
    canActivate: [guestGuard]
  },
  {
    path: '',
    component: Layout,
    canActivate: [authGuard],
    canActivateChild: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        component: Dashboard
      },
      {
        path: 'employees',
        component: Employees
      },
      {
        path: 'departments',
        component: Departments,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HR_MANAGER'] }
      },
      {
        path: 'jobs',
        component: Jobs
      },
      {
        path: 'candidates',
        component: Candidates,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HR_MANAGER'] }
      },
      {
        path: 'interviews',
        component: Interviews,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HR_MANAGER'] }
      },
      {
        path: 'attendance',
        component: Attendance
      },
      {
        path: 'leaves',
        component: Leaves
      },
      {
        path: 'payroll',
        component: Payroll
      },
      {
        path: 'performance',
        component: Performance
      },
      {
        path: 'training',
        component: Training
      },
      {
        path: 'documents',
        component: Documents
      },
      {
        path: 'users',
        component: Users,
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] }
      },
      {
        path: 'profile',
        component: Profile
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];