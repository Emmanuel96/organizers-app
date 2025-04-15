declare const bootstrap: any;

import { Component, OnDestroy, OnInit, ViewChild, inject, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';

import { CalendarOptions, EventClickArg } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import { FullCalendarComponent, FullCalendarModule } from '@fullcalendar/angular';
import { EventService } from 'app/entities/event/service/event.service';
import { FormsModule } from '@angular/forms';

interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  token_type: string;
}

@Component({
  standalone: true,
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [SharedModule, RouterModule, FullCalendarModule, FormsModule],
})
export default class HomeComponent implements OnInit, OnDestroy {
  groupedEvents: any[] = [];
  eventTitle = '';
  eventDescription = '';
  eventDate = '';
  eventLocation = '';
  eventUrl = '';
  eventGroup: any;
  events: any[] = [];

  account = signal<Account | null>(null);
  @ViewChild('calendar') calendarComponent: FullCalendarComponent | undefined;

  groupName = '';
  code: string | null = '';

  clientId: any = '2qff8uujb6qsbo2rnarmh5p2du';
  clientSecret = 'krbnaft7ri8vaqbtpteo4s9ne0';
  redirectUri = 'https://organizer-app-140b2a7a7c09.herokuapp.com/';
  tokenUrl = 'https://secure.meetup.com/oauth2/access';
  calendarOptions: CalendarOptions = {
    initialView: 'dayGridMonth',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: '', // 'customButton',
    },
    plugins: [dayGridPlugin, interactionPlugin],
    dateClick: () => this.handleDateClick(),
    weekends: true,
    customButtons: {
      customButton: {
        // text: 'Add Group',
        // click: () => this.openModal(),
      },
    },
    events: [],
    eventClick: this.handleEventClick.bind(this),
  };

  private readonly destroy$ = new Subject<void>();

  private accountService = inject(AccountService);
  private router = inject(Router);
  private eventService = inject(EventService);

  // constructor() {
  //   // this.groupEventsByMonth();
  // }

  handleEventClick(clickInfo: EventClickArg): void {
    const id = clickInfo.event.id;

    const event: any = this.events.find((e: any) => e.id.toString() === id);

    if (event) {
      this.eventDescription = event.event_description;
      this.eventDate = this.convertToMountainTime(event.event_date);
      this.eventLocation = event.event_location ? event.event_location : 'Online Event';
      this.eventGroup = event.eventGroupDisplayName;
      this.eventUrl = event.event_url;
      this.eventTitle = event.title;
    }

    this.openModal();
  }

  handleMobileEvent(selectedEvent: any): void {
    const event: any = selectedEvent;
    // eslint-disable-next-line no-console
    console.info('event:', event);

    if (event) {
      this.eventDescription = event.event_description;
      this.eventDate = this.convertToMountainTime(event.event_date);
      this.eventLocation = event.event_location ? event.event_location : 'Online Event';
      this.eventGroup = event.eventGroupDisplayName;
      this.eventUrl = event.event_url;
      // eslint-disable-next-line no-console
      console.info('eventUrl:', this.eventUrl);
    }

    this.openModal();
  }

  public toggleWeekends(): void {
    this.calendarOptions.weekends = !this.calendarOptions.weekends;
  }
  handleDateClick(): void {
    const testVar = 0;
  }

  ngOnInit(): void {
    const urlParams = new URLSearchParams(window.location.search);
    this.code = urlParams.get('code');

    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => this.account.set(account));

    // eslint-disable-next-line no-console
    console.info('session storage:', sessionStorage.getItem('access_token'));
    this.loadEvents();

    // eslint-disable-next-line no-console
    console.info('this.events"', this.events);
  }

  async getAccessToken(code: string): Promise<void> {
    try {
      const response = await fetch(this.tokenUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          client_id: this.clientId,
          client_secret: this.clientSecret,
          grant_type: 'authorization_code',
          redirect_uri: this.redirectUri,
          code,
        }).toString(),
      });
      if (!response.ok) {
        throw new Error(`Error: ${response.status} ${response.statusText}`);
      }
      const data: TokenResponse = await response.json();
      // eslint-disable-next-line no-console
      console.info('Access Token:', data.access_token);
      sessionStorage.setItem('access_token', data.access_token);
      sessionStorage.setItem('refresh_token', data.refresh_token);
      // eslint-disable-next-line no-console
      console.info('Refresh Token:', data.refresh_token);
    } catch (error) {
      console.error('Failed to fetch access token:', error);
    }
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  openModal(): void {
    const modalElement = document.getElementById('groupNameModal');
    if (modalElement) {
      const modal = new bootstrap.Modal(modalElement);
      modal.show();
    }
  }

  close(): void {
    const modalElement = document.getElementById('groupNameModal') as HTMLElement;
    const modal = bootstrap.Modal.getInstance(modalElement);
    if (modal) {
      // eslint-disable-next-line no-console
      console.info('Modal:', modal);
      modal.hide();

      const backdrops = document.querySelectorAll('.modal-backdrop');
      backdrops.forEach(b => b.remove());
    }
  }

  submitGroupName(): void {
    // eslint-disable-next-line no-console
    console.log('Code:', this.code);
    // eslint-disable-next-line no-console
    console.log('Group Name:', this.groupName);
    const modalElement = document.getElementById('groupNameModal') as HTMLElement;

    // get group name
    if (this.code) {
      // pass the code
      this.eventService.getEventByGroupName(this.code, this.groupName).subscribe(events => {
        // eslint-disable-next-line no-console
        console.log('events: ', events);
      });
    } else {
      // eslint-disable-next-line no-console
      console.log('No code');
    }
    const modal = bootstrap.Modal.getInstance(modalElement);
    modal.hide();

    // now pass that group name to the service
  }

  convertToMountainTime(gmtDateString: string): string {
    // Create a Date object from the GMT date string
    const gmtDate = new Date(gmtDateString);

    // Format the date to Mountain Time (America/Denver)
    return gmtDate.toLocaleString('en-US', {
      timeZone: 'America/Denver',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: 'numeric',
      minute: 'numeric',
      second: 'numeric',
    });
  }

  loadEvents(): void {
    this.eventService
      .query()
      .pipe(takeUntil(this.destroy$))
      .subscribe(events => {
        this.events = events.body ?? [];
        this.calendarOptions.events = events.body?.map(value => {
          // Determine event source and assign a color
          let eventColor = '#3788d8'; // default color
          if (value.eventbriteOrganizerId && value.eventbriteOrganizerId.trim() !== '') {
            eventColor = '#FF5733';
          } else if (value.eventGroupName && value.eventGroupName.trim() !== '') {
            eventColor = '#E51937';
          }
          return {
            id: value.id.toString(),
            // Ensure that event_date is formatted correctly
            date: value.event_date ? value.event_date.format('YYYY-MM-DD').toString() : '',
            title: value.eventTitle ?? '',
            groupName: value.eventGroupName,
            event_url: value.event_url,
            event_group_display_name: value.eventGroupDisplayName,
            backgroundColor: eventColor,
            borderColor: eventColor,
          };
        });
        this.groupEventsByMonth();
      });
  }

  groupEventsByMonth(): { month: string; events: any[] }[] {
    const today = new Date();

    let futureEvents = this.events.filter(event => new Date(event.event_date) >= today);

    futureEvents = futureEvents
      .filter(event => new Date(event.event_date) >= today)
      .sort((a, b) => new Date(a.event_date).getTime() - new Date(b.event_date).getTime());

    const grouped = futureEvents.reduce((acc: Record<string, any[]>, event: any) => {
      const eventDate = new Date(event.event_date);
      const month = eventDate.toLocaleString('default', { month: 'long' });

      // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
      if (!acc[month]) {
        acc[month] = [];
      }
      acc[month].push(event);

      return acc;
    }, {});

    this.groupedEvents = Object.keys(grouped).map(month => ({
      month,
      events: grouped[month],
    }));

    // eslint-disable-next-line @typescript-eslint/no-unsafe-return
    return this.groupedEvents;
  }

  isSameDay(date1: string | Date, date2: string | Date): boolean {
    const d1 = new Date(date1);
    const d2 = new Date(date2);
    return d1.getFullYear() === d2.getFullYear() && d1.getMonth() === d2.getMonth() && d1.getDate() === d2.getDate();
  }
}
