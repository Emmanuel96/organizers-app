import { Component, OnDestroy, OnInit, ViewChild, inject, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';

import { CalendarOptions } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import { FullCalendarComponent, FullCalendarModule } from '@fullcalendar/angular';
import { EventService } from 'app/entities/event/service/event.service';

@Component({
  standalone: true,
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [SharedModule, RouterModule, FullCalendarModule],
})
export default class HomeComponent implements OnInit, OnDestroy {
  account = signal<Account | null>(null);
  @ViewChild('calendar') calendarComponent: FullCalendarComponent | undefined;

  calendarOptions: CalendarOptions = {
    initialView: 'dayGridMonth',
    plugins: [dayGridPlugin, interactionPlugin],
    dateClick: () => this.handleDateClick(),
    weekends: true,
    events: [],
  };

  private readonly destroy$ = new Subject<void>();

  private accountService = inject(AccountService);
  private router = inject(Router);
  private eventService = inject(EventService);

  public toggleWeekends(): void {
    this.calendarOptions.weekends = !this.calendarOptions.weekends;
  }
  handleDateClick(): void {
    const testVar = 0;
  }

  ngOnInit(): void {
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => this.account.set(account));

    this.loadEvents();
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadEvents(): void {
    this.eventService
      .query()
      .pipe(takeUntil(this.destroy$))
      .subscribe(events => {
        this.calendarOptions.events = events.body?.map(value => {
          return {
            id: value.id.toString(),
            date: value.event_date?.format('YYYY-MM-DD').toString(),
            title: value.event_description ?? '',
          };
        });
      });
  }
}
