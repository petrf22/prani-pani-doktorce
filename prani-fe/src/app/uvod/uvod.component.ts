import { Component, inject } from '@angular/core';
import { MailComponent } from "../mail/mail.component";
import { UserService } from '../services/user-service';
import { RouterLink } from '@angular/router';
import { NzSpaceModule } from 'ng-zorro-antd/space';

@Component({
  selector: 'app-uvod',
  imports: [MailComponent, RouterLink, NzSpaceModule],
  templateUrl: './uvod.component.html',
  styleUrl: './uvod.component.scss'
})
export class UvodComponent {
  protected userService = inject(UserService);
}
