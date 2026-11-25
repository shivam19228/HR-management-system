import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HrDataService, EmployeeItem } from './hr-data';

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  constructor(private hrData: HrDataService) {}

  getEmployees(): Observable<EmployeeItem[]> {
    return of(this.hrData.getEmployees());
  }

  addEmployee(employee: any): Observable<EmployeeItem> {
    const created = this.hrData.addEmployee(employee);
    return of(created);
  }

  updateEmployee(id: number, employee: any): Observable<EmployeeItem | null> {
    const updated = this.hrData.updateEmployee(id, employee);
    return of(updated);
  }

  deleteEmployee(id: number): Observable<boolean> {
    const success = this.hrData.deleteEmployee(id);
    return of(success);
  }
}